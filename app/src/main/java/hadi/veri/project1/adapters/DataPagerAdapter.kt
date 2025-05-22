package hadi.veri.project1.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import hadi.veri.project1.fragments.CardStokFragment
import hadi.veri.project1.fragments.MasterStokFragment
import hadi.veri.project1.fragments.MutasiStokFragment
import hadi.veri.project1.fragments.PesananUserFragment

class DataPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val userRole: String
) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MasterStokFragment().apply {
                arguments = androidx.core.os.bundleOf("role" to userRole)
            }
            1 -> CardStokFragment().apply {
                arguments = androidx.core.os.bundleOf("role" to userRole)
            }
            2 -> MutasiStokFragment().apply {
                arguments = androidx.core.os.bundleOf("role" to userRole)
            }
            3 -> PesananUserFragment().apply {
                arguments = androidx.core.os.bundleOf("role" to userRole)
            }
            else -> MasterStokFragment().apply {
                arguments = androidx.core.os.bundleOf("role" to userRole)
            }
        }
    }
} 