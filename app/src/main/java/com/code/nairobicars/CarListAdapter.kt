package com.code.nairobicars

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide

// Adapter binds the data (car details and images) to the recyclerView
class CarListAdapter(
    private var carList: MutableList<Car>, // Make carList mutable
    private val context: Context
) : RecyclerView.Adapter<CarListAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager = itemView.findViewById(R.id.viewPagerCarImages)
        val carMakeModelTextView: TextView = itemView.findViewById(R.id.carMakeModelTextView)
        val carDetailsTextView: TextView = itemView.findViewById(R.id.carDetailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_listing, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]

        // Set car make and model
        holder.carMakeModelTextView.text = "${car.make} ${car.model}"

        // Set car details (Year and Price)
        holder.carDetailsTextView.text = "Year: ${car.year}, Price: ${car.price}"

        // Set up ViewPager for car images
        val imagePagerAdapter = ImagePagerAdapter(context, car.images)
        holder.viewPager.adapter = imagePagerAdapter
    }

    override fun getItemCount(): Int {
        return carList.size
    }

    // Method to update carList
    fun updateCarList(newCarList: List<Car>) {
        carList.clear()
        carList.addAll(newCarList)
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    // Adapter for the ViewPager (car images)
    class ImagePagerAdapter(private val context: Context, private val images: List<String>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            // Load image using Glide
            Glide.with(context).load(images[position]).into(imageView)

            container.addView(imageView)
            return imageView
        }

        override fun getCount(): Int = images.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}
