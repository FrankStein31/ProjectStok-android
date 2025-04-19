package hadi.veri.project1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hadi.veri.project1.R
import hadi.veri.project1.databinding.FragmentDataBinding

class DataFragment : Fragment() {
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCardClickListeners()
    }

    private fun setupCardClickListeners() {
        // Navigasi ke Master Stok
        binding.cardDataBarang.setOnClickListener {
            navigateToFragment(MasterStokFragment.newInstance())
        }
        
        // Navigasi ke Mutasi Stok
        binding.cardInputBarang.setOnClickListener {
            navigateToFragment(MutasiStokFragment.newInstance())
        }
        
        // Navigasi ke Card Stok
        binding.cardHistory.setOnClickListener {
            navigateToFragment(CardStokFragment.newInstance())
        }
        
        // Navigasi ke Pesanan User
        binding.cardStatistik.setOnClickListener {
            navigateToFragment(PesananUserFragment.newInstance())
        }
        
        // Navigasi ke Manajemen User (bukan Profile)
        binding.cardUser.setOnClickListener {
            navigateToFragment(UsersFragment.newInstance())
        }
    }
    
    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = DataFragment()
    }
} 