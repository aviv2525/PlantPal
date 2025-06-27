package com.example.plantpal.ui.details

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.plantpal.viewmodel.FavoriteViewModel
import com.example.plantpal.model.Plant
import com.example.plantpal.viewmodel.PlantViewModel
import com.example.plantpal.R
import com.example.plantpal.databinding.FragmentPlantDetailBinding
import com.example.plantpal.util.Resource
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlin.collections.joinToString

import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class PlantDetailFragment : Fragment() {

    private var _binding: FragmentPlantDetailBinding? = null
    private val binding get() = _binding!!

    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val viewModel: PlantViewModel by viewModels()
    private val args: PlantDetailFragmentArgs by navArgs()

    private var plantId: Int = -1
    private var currentImageUrl: String? = null
    private var wateringRaw: String? = null
    private var sunlightRaw: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plant = args.plant
        plantId = plant.id
        Log.d("DETAILS_DEBUG", "Sending plantId: $plantId")

        binding.tvPlantName.text = plant.commonName ?: getString(R.string.unknown_plant)
        binding.tvScientificName.text = getString(R.string.scientific_name) + plant.scientificName?.joinToString(", ") ?: "N/A"

        binding.tvWateringInfo.text = buildString {
            append(binding.tvWateringInfo.text)
            append("Loading. . .")
        }
       binding.tvSunlightInfo.text = buildString {
            append(binding.tvSunlightInfo.text)
            append("Loading. . .")
        }



        // הצגת תמונה
        val imageUrl = plant.defaultImage?.originalUrl

        val imageToLoad = if (!imageUrl.isNullOrBlank()) {
            imageUrl
        } else {
            R.drawable.plantpal_icon
        }

        Glide.with(requireContext())
            .load(imageToLoad)
            .centerCrop()
            .into(binding.ivPlantImage)


        binding.ivPlantImage.setOnClickListener {
            showImageFullScreen(imageUrl ?: R.drawable.plantpal_icon)
        }




        // שליפת מידע נוסף מה-API
        viewModel.fetchPlantDetails(plantId)
        viewModel.plantDetails.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val details = state.data

                    val wateringOptions = listOf(
                        getString(R.string.frequent),
                        getString(R.string.average),
                        getString(R.string.minimum),
                        getString(R.string.none))
                    wateringRaw = details.watering
                    val wateringText = when (wateringRaw?.lowercase()) {
                        "frequent" -> "💧💧💧 frequent"
                        "average" -> "💧💧 average"
                        "minimum" -> "💧 minimum "
                        "none" -> "🚫 No water needed"
                        else -> " "
                    }

                    binding.tvWateringInfo.text = getString(R.string.watering_info) + wateringText




                    val sunlightOptions =
                        listOf(
                            getString(R.string.full_sun),
                            getString(R.string.part_shade),
                            getString(R.string.full_shade),
                            getString(R.string.sun_part_shade))

                    val sunlightTexts = details.sunlight?.map { sunlightValue ->
                        when (sunlightValue.lowercase()) {
                            "full sun" -> "☀️ full sun"
                            "part shade" -> "🌤 part shade"
                            "sun-part shade" -> "⛅ sun part shade"
                            "filtered shade" -> "🌫 filtered shade"
                            "full shade" -> "🌑 full shade"
                            else -> "❓ $sunlightValue"
                        }
                    } ?: listOf("❓ unknown")

                    sunlightRaw = details.sunlight?.joinToString ( " | " )
                    binding.tvSunlightInfo.text = getString(R.string.sunlight_info) + sunlightTexts.joinToString(" | ")


                    // טעינת תמונה מעודכנת אם קיימת
                    val updatedImage = details.defaultImage?.original_url
                    if (!updatedImage.isNullOrBlank()) {
                        currentImageUrl = updatedImage
                        Glide.with(requireContext())
                            .load(updatedImage)
                            .centerCrop()
                            .into(binding.ivPlantImage)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.btnSaveFullPlant.setOnClickListener {
            favoriteViewModel.addFavorite(
                Plant(
                    id = plantId,
                    commonName = binding.tvPlantName.text.toString(),
                    scientificName = binding.tvScientificName.text.toString(),
                    imageUrl = currentImageUrl,
                    watering = binding.tvWateringInfo.text.toString(),
                    sunlight = binding.tvSunlightInfo.text.toString()
                )
            )
            Toast.makeText(requireContext(), "Save Successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnViewCareTips.setOnClickListener {
            Snackbar.make(
                binding.root,
                getString(R.string.to_set_watering_reminders_please_add_this_plant_to_your_favorites),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.add_now)) {
                favoriteViewModel.addFavorite(
                    Plant(
                        id = plantId,
                        commonName = binding.tvPlantName.text.toString(),
                        scientificName = binding.tvScientificName.text.toString(),
                        imageUrl = currentImageUrl,
                        watering = binding.tvWateringInfo.text.toString(),
                        sunlight = binding.tvSunlightInfo.text.toString()
                    )
                )
                Toast.makeText(requireContext(),
                    getString(R.string.save_successfully), Toast.LENGTH_SHORT).show()            }.show()
        }


    }



    private fun showImageFullScreen(imageSource: Any) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_fullscreen_image)

        val imageView = dialog.findViewById<ImageView>(R.id.fullscreenImageView)

        Glide.with(requireContext())
            .load(imageSource)
            .into(imageView)

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
