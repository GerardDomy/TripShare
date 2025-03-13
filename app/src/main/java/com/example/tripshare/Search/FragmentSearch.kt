package com.example.tripshare.Search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentSearch : Fragment() {

    private lateinit var userAdapter: UserAdapter
    private var mUsers: ArrayList<User> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.searchList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchUsersET)

        recyclerView.visibility = View.GONE

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                if (cs.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    searchForUser(cs.toString().lowercase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun searchForUser(query: String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                mUsers.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    if (user.userId != firebaseUserID) {
                        mUsers.add(user)
                    }
                }
                if (mUsers.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.GONE
                }
                userAdapter = UserAdapter(requireContext(), mUsers)
                recyclerView.adapter = userAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

