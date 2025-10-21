package com.example.medikit

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val rv = findViewById<RecyclerView>(R.id.rvHistorial)
        val emptyState = findViewById<View>(R.id.emptyState)
        val btnVolver = findViewById<View>(R.id.btnVolver)
        val btnEmptyCaptura = findViewById<View>(R.id.btnEmptyCaptura)
        val btnEmptyHome = findViewById<View>(R.id.btnEmptyHome)

        val registros = AnalisisStorage
            .leerTodos(this)
            .sortedByDescending { it.timestamp }
            .toMutableList()

        fun actualizarVistas() {
            if (registros.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                rv.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                rv.visibility = View.VISIBLE
            }
        }

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = HistorialAdapter(registros) { registro ->
            ImagenGlobal.rutaImagen = registro.rutaImagen
            ImagenGlobal.resultadoIA = Detector.ResultadoIA(registro.clase, registro.confianza)
            startActivity(Intent(this, ResultadoActivity::class.java))
        }
        rv.adapter = adapter

        actualizarVistas()

        // Botones de navegación
        btnVolver.setOnClickListener { finish() }
        btnEmptyCaptura.setOnClickListener {
            startActivity(Intent(this, CapturaActivity::class.java))
            finish()
        }
        btnEmptyHome.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        // Swipe para eliminar con confirmación
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = registros.getOrNull(position)
                if (item == null) {
                    adapter.notifyItemChanged(position)
                    return
                }

                AlertDialog.Builder(this@HistorialActivity)
                    .setTitle("Eliminar análisis")
                    .setMessage("¿Deseas eliminar este registro del historial?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        val ok = AnalisisStorage.eliminarPorTimestamp(this@HistorialActivity, item.timestamp)
                        if (ok) {
                            registros.removeAt(position)
                            adapter.notifyItemRemoved(position)
                            actualizarVistas()
                        } else {
                            adapter.notifyItemChanged(position)
                        }
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(rv)
    }

    class HistorialAdapter(
        private val items: MutableList<AnalisisStorage.RegistroAnalisis>,
        private val onClick: (AnalisisStorage.RegistroAnalisis) -> Unit
    ) : RecyclerView.Adapter<HistorialAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView = v.findViewById(R.id.imgMiniatura)
            val tvClase: TextView = v.findViewById(R.id.tvClase)
            val tvFecha: TextView = v.findViewById(R.id.tvFecha)
            val tvConfianza: TextView = v.findViewById(R.id.tvConfianza)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvClase.text = item.clase
            holder.tvFecha.text = item.fecha
            holder.tvConfianza.text = "${(item.confianza * 100).toInt()}%"

            // Miniatura
            try {
                val bmp = BitmapFactory.decodeFile(item.rutaImagen)
                if (bmp != null) holder.img.setImageBitmap(bmp)
                else holder.img.setImageResource(R.drawable.baseline_camera_alt_24)
            } catch (_: Exception) {
                holder.img.setImageResource(R.drawable.baseline_camera_alt_24)
            }

            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
