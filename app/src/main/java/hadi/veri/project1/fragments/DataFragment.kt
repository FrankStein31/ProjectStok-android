package hadi.veri.project1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import hadi.veri.project1.R
import hadi.veri.project1.databinding.FragmentDataBinding

class DataFragment : Fragment() {
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!
    private var userRole: String? = null // Role pengguna

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role") // Default ke "user" jika null
            Toast.makeText(requireContext(), "Role di FragmentData: $userRole", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Batasi akses berdasarkan role
        if (userRole.equals("User", ignoreCase = true)) {
            binding.cardHistory.visibility = View.GONE
            binding.cardInputBarang.visibility = View.GONE
            binding.cardUser.visibility = View.GONE
            binding.cardDataBarang.visibility = View.GONE
        }
        setupCardClickListeners()
    }

    private fun setupCardClickListeners() {
        // Navigasi ke Master Stok
        binding.cardDataBarang.setOnClickListener {
            navigateToFragment(MasterStokFragment.newInstance(), userRole)
        }

        // Navigasi ke Mutasi Stok
        binding.cardInputBarang.setOnClickListener {
            navigateToFragment(MutasiStokFragment.newInstance(), userRole)
        }

        // Navigasi ke Card Stok
        binding.cardHistory.setOnClickListener {
            navigateToFragment(CardStokFragment.newInstance(), userRole)
        }

        // Navigasi ke Pesanan User
        binding.cardStatistik.setOnClickListener {
            navigateToFragment(PesananUserFragment.newInstance(), userRole)
        }

        // Navigasi ke Manajemen User (bukan Profile)
        binding.cardUser.setOnClickListener {
            navigateToFragment(UsersFragment.newInstance(), userRole)
        }
    }

    private fun navigateToFragment(fragment: Fragment, role: String?) {
        // Buat bundle untuk mengirim role
        val bundle = Bundle()
        bundle.putString("role", role)
        fragment.arguments = bundle // Pasang bundle ke fragment tujuan

        // Navigasikan ke fragment tujuan
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)  // Gunakan 'replace' untuk mengganti fragment
            .addToBackStack(null)  // Menambahkan fragment ke back stack
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
