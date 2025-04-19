package hadi.veri.project1.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import hadi.veri.project1.fragments.CardStokFragment
import hadi.veri.project1.fragments.MasterStokFragment
import hadi.veri.project1.fragments.MutasiStokFragment
import hadi.veri.project1.fragments.PesananUserFragment

class DataFragmentPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : 
    FragmentStateAdapter(fragmentManager, lifecycle) {
    
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MasterStokFragment.newInstance()
            1 -> CardStokFragment.newInstance()
            2 -> MutasiStokFragment.newInstance()
            3 -> PesananUserFragment.newInstance()
            else -> MasterStokFragment.newInstance()
        }
    }
} 