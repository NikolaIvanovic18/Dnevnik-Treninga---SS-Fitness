package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import java.util.*
import androidx.core.content.edit

class SettingsFragment : Fragment() {

    private val PREFS_NAME = "app_prefs"
    private val KEY_LANGUAGE = "language"
    private val KEY_THEME = "theme"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val languageSpinner: Spinner = view.findViewById(R.id.language_spinner)
        val themeSpinner: Spinner = view.findViewById(R.id.theme_spinner)

        // Language spinner
        val languages = listOf("Srpski", "English")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, languages)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANGUAGE, Locale.getDefault().language)
        languageSpinner.setSelection(if (savedLang == "sr") 0 else 1)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val langCode = if (position == 0) "sr" else "en"
                if (savedLang != langCode) {
                    prefs.edit { putString(KEY_LANGUAGE, langCode) }
                    setLocale(requireContext(), langCode)
                    activity?.recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Theme spinner
        val themeOptions = listOf(
            getString(R.string.theme_follow_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val themeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, themeOptions)
        themeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter

        val savedTheme = prefs.getString(KEY_THEME, "system")
        themeSpinner.setSelection(
            when (savedTheme) {
                "light" -> 1
                "dark" -> 2
                else -> 0
            }
        )

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val mode = when (position) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                val themeValue = when (position) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                if (savedTheme != themeValue) {
                    prefs.edit { putString(KEY_THEME, themeValue) }
                    AppCompatDelegate.setDefaultNightMode(mode)
                    activity?.recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        languageSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.color.white))
        themeSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.color.white))

        fun tintSpinnerArrow(spinner: Spinner) {
            val drawable = spinner.background
            drawable?.let {
                DrawableCompat.setTint(DrawableCompat.wrap(it), ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        tintSpinnerArrow(languageSpinner)
        tintSpinnerArrow(themeSpinner)

        return view
    }

    private fun setLocale(context: Context, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}