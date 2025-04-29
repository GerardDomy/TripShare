package com.example.tripshare.Search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson

class FragmentSearch : Fragment() {

    private lateinit var userAdapter: UserAdapter
    private val mUsers = mutableListOf<User>()
    private val visitedUsers = mutableListOf<User>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        setupViews(view)
        setupRecyclerView()
        setupSearchListener()
        loadVisibilityState()
        return view
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.searchList)
        searchEditText = view.findViewById(R.id.searchUsersET)
    }

    private fun setupRecyclerView(isSearching: Boolean = false) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(
            requireContext(),
            mUsers,
            { user ->
                addVisitedUser(user)
                navigateToProfile(user)
            },
            showDeleteButton = !isSearching // El botón se muestra solo cuando NO hay búsqueda
        )
        recyclerView.adapter = userAdapter
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                if (cs.isNullOrEmpty()) {
                    showVisitedUsers()
                    setupRecyclerView(false) // Reactivar el botón
                } else {
                    searchForUser(cs.toString())
                    setupRecyclerView(true) // Desactivar el botón
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchForUser(query: String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereGreaterThanOrEqualTo("name", query.lowercase())
            .get()
            .addOnSuccessListener { documents ->
                mUsers.clear()
                documents.forEach { document ->
                    if (document.id != firebaseUserID) {
                        val user = User(
                            userId = document.id,
                            name = document.getString("name") ?: "",
                            imageUri = document.getString("imageUrl") ?: ""
                        )
                        mUsers.add(user)
                    }
                }
                updateRecyclerViewVisibility(mUsers)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addVisitedUser(user: User) {
        // Eliminar si ya estaba y volver a agregar al principio
        visitedUsers.removeAll { it.userId == user.userId }
        visitedUsers.add(0, user)

        saveVisitedUserToFirestore(user)
        updateRecyclerViewVisibility(visitedUsers)
    }

    private fun showVisitedUsers() {
        mUsers.clear()
        mUsers.addAll(visitedUsers)
        updateRecyclerViewVisibility(mUsers)
    }

    private fun updateRecyclerViewVisibility(users: List<User>) {
        val hasResults = users.isNotEmpty()
        recyclerView.visibility = if (hasResults) View.VISIBLE else View.GONE
        userAdapter.notifyDataSetChanged()

        // Solo guardar estado si hay resultados de búsqueda
        if (!searchEditText.text.isNullOrEmpty()) {
            saveVisibilityState(hasResults)
        }
    }

    private fun saveVisibilityState(visible: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .update("uiSettings.searchVisible", visible)
            .addOnFailureListener { e ->
                Log.e("FragmentSearch", "Error saving visibility state", e)
            }
    }

    private fun navigateToProfile(user: User) {
        startActivity(Intent(requireContext(), ProfileActivity::class.java).apply {
            putExtra("USER_NAME", user.name)
            putExtra("USER_ID", user.userId)
        })
    }



    // Persistencia en SharedPreferences (Opcional)
    private fun saveVisitedUsers() {
        val sharedPref = requireContext().getSharedPreferences("visited_users", Context.MODE_PRIVATE)
        val json = Gson().toJson(visitedUsers)
        sharedPref.edit().putString("visited_users_list", json).apply()
    }

    private fun saveVisitedUserToFirestore(user: User) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val visitedUserData = hashMapOf(
            "name" to user.name,
            "imageUri" to user.imageUri,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(currentUser)
            .collection("visitedUsers")
            .document(user.userId)
            .set(visitedUserData)
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error guardando usuario visitado", e)
            }
    }

    private fun loadVisitedUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUser)
            .collection("visitedUsers")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                visitedUsers.clear()
                documents.forEach { doc ->
                    visitedUsers.add(User(
                        userId = doc.id,
                        name = doc.getString("name") ?: "",
                        imageUri = doc.getString("imageUri") ?: ""
                    ))
                }
                showVisitedUsers()
            }
    }

    private fun loadVisibilityState() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val settings = document.get("uiSettings") as? Map<String, Any>
                val visible = settings?.get("searchVisible") as? Boolean ?: true
                recyclerView.visibility = if (visible) View.VISIBLE else View.GONE
            }
    }

    override fun onResume() {
        super.onResume()
        if (searchEditText.text.isNullOrEmpty()) {
            loadVisitedUsers() // Cargar visitados al mostrar el fragmento
        }
    }
}
