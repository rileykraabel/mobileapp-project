package com.example.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await

external fun brightness(bmp: Bitmap?, brightness: Float)
external fun invertColors(bmp: Bitmap?)
enum class LoginState {
    NotLogged, SigningUp, LoggedIn
}

data class Drawing(val bitmap: Bitmap, val dPath: DrawingPath)
class DrawableViewModel(private val repository: DrawingRepository) : ViewModel() {
    companion object {
        init {
            System.loadLibrary("drawable")
        }
    }

    //new implementation
    val drawings: Flow<List<Drawing>> = repository.drawings
    val count: Flow<Int> = repository.count
    val usernameFlow = MutableStateFlow<String>("")

    class Factory(private val repository: DrawingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DrawableViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DrawableViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val bitmapLiveData = MutableLiveData<Bitmap>()
    var currBitmap = bitmapLiveData as LiveData<out Bitmap>
    private val saveColor = MutableLiveData<Int>(Color.BLACK)
    var currColor = saveColor as LiveData<out Int>
    private val _state = MutableStateFlow<LoginState>(LoginState.NotLogged)
    val state: StateFlow<LoginState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
        updateLoginState(firebaseAuth.currentUser)
    }

    init {
        auth.addAuthStateListener(authStateListener)

    }


    fun login() {
        _state.value = LoginState.LoggedIn // Update state when logged in
    }

    fun startSignUp() {
        _state.value = LoginState.SigningUp // Update state when starting sign up
    }

    fun signOut() {
        _state.value = LoginState.NotLogged // Update state when signed out
    }

    /**
     * Adds drawing to list
     * @param drawing: The drawing we are inserting into the List
     */
    fun add(drawing: Drawing) {
        viewModelScope.launch {
            repository.saveDrawing(drawing)
        }
    }

    /**
     * Removes drawing from list
     * @param dpath: The drawing path of the file to delete
     */
    fun removeDrawing(dpath: DrawingPath) {
        viewModelScope.launch {
            repository.deleteDrawing(dpath)
        }
    }

    /**
     *
     */
    suspend fun checkForDrawing(name: String): Boolean {
        return repository.nameCheck(name)
    }

    /**
     * Updates the color when its changed
     * @param color: the new color
     */
    fun updateColor(color: Int) {
        saveColor.value = color
        saveColor.value = saveColor.value
    }

    /**
     * Updates the current bitmap when changes happen
     * @param bitmap: the changed bitmap
     */
    fun updateBitmap(bitmap: Bitmap) {
        bitmapLiveData.value = bitmap
        bitmapLiveData.value = bitmapLiveData.value
    }

    /**
     * Sets the current bitmap
     * @param dpath: The drawing path of file to set as current bitmap
     */
    fun setCurrBitmap(dpath: DrawingPath) {
        val drawing = repository.loadDrawing(dpath)
        bitmapLiveData.value = drawing.bitmap
        bitmapLiveData.value = bitmapLiveData.value
    }

    /**
     *
     */
    fun brightenImage() {
        val currentBitmap = bitmapLiveData.value
        if (currentBitmap != null) {
            brightness(currentBitmap, .25F)
            updateBitmap(currentBitmap)
        }
    }

    /**
     *
     */
    fun invertColors() {
        val currentBitmap = bitmapLiveData.value
        if (currentBitmap != null) {
            invertColors(currentBitmap)
            updateBitmap(currentBitmap)

        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clearDatabase()
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun updateLoginState(user: FirebaseUser?) {
        _state.value = when (user) {
            null -> LoginState.NotLogged
            else -> LoginState.LoggedIn
        }
    }


    fun log_in(
        email: String,
        password: String,
        letEmKnow: (String) -> Unit,
        context: Context,
        onSignIn: () -> Unit
    ) {
        // Sign in and wait for completion
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _currentUser.value = Firebase.auth.currentUser

                // Once user is logged in, get their username
                Firebase.firestore.collection("users").document(_currentUser.value!!.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val username = document.getString("username")  // Retrieve the username
                            letEmKnow("USERNAME RETRIEVED: $username")
                            usernameFlow.value = username!!  // Update the ViewModel or state holder
                        } else {
                            letEmKnow("No such document!")
                        }
                    }
                    .addOnFailureListener { exception ->
                        letEmKnow("Error fetching document: ${exception.message}")
                    }


                viewModelScope.launch {

                    val userId = _currentUser.value!!.uid  // Ensure safe unwrapping in production code
                    val userFolder = Firebase.storage.reference.child(userId)
                    getDrawingsFromFirebase(userFolder, letEmKnow, onSignIn)

                }
            }
            .addOnFailureListener { e ->
                letEmKnow("Failed to retrieve data")
            }


    }

    private fun getDrawingsFromFirebase(
        imagesRef: StorageReference,
        letEmKnow: (String) -> Unit,
        onSignIn: () -> Unit
    ) {

        val options = BitmapFactory.Options().apply {
            inMutable = true
        }

        imagesRef.listAll()
            .addOnSuccessListener { result ->
                result.items.forEach { fileRef ->

                    fileRef.metadata.addOnSuccessListener { metadata ->
                        val lastModifiedDate = metadata.updatedTimeMillis
                        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                            letEmKnow("Download successful for ${fileRef.name} last modified on $lastModifiedDate")
                            val bitmap =
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                            val drawing =
                                Drawing(bitmap, DrawingPath(lastModifiedDate, fileRef.name))
                            add(drawing)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                letEmKnow("Failed to list files: $e")
            }
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    letEmKnow("Data was successfully downloaded!")
                    onSignIn()
                } else {
                    letEmKnow(("Failed to download: ${task.exception?.message}"))
                }
            }
    }


    fun register(
        username: String,
        email: String,
        password: String,
        letEmKnow: (String) -> Unit,
        onSignUpClicked: () -> Unit
    ) {
        usernameFlow.value = username
        viewModelScope.launch {
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { _ ->
                    _currentUser.value = Firebase.auth.currentUser
                    val userDoc = Firebase.firestore.collection("Usernames")
                        .document(Firebase.auth.currentUser!!.uid)
                    val drawableUser = DrawableUser(username)
                    userDoc.set(drawableUser)

                    onSignUpClicked()
                }
                .addOnFailureListener { _ ->
                    letEmKnow("Failed while signing up")
                }
        }

    }

    fun update_user(
        username: String,
        email: String,
        unChanged: Boolean,
        emChanged: Boolean,
        letEmKnow: (String) -> Unit
    ) {
        if (emChanged) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _currentUser.value!!.verifyBeforeUpdateEmail(email)
                    .addOnSuccessListener { _ ->
                        letEmKnow("Email address successfully changed :)") // doesnt work lol
                    }
            } else {
                letEmKnow("Enter a valid email address >:(")
            }
        }

        if (unChanged) {
            val userUpdates = mapOf(
                "username" to username
            )
            Firebase.firestore.collection("users/")
                .document(_currentUser.value!!.uid)
                .update(userUpdates)
                .addOnSuccessListener {
                    letEmKnow("Username successfully changed!")
                    usernameFlow.value = username
                }
                .addOnFailureListener { e ->
                    letEmKnow("Failed to change username :( ${e.message}")
                }
        }
    }

    fun sign_out(letEmKnow: (String) -> Unit, onSignOut: () -> Unit) {

        Firebase.firestore.collection("users/").document(_currentUser.value!!.uid)
            .set(mapOf("username" to usernameFlow.value!!))
            .addOnSuccessListener { letEmKnow("USERNAME UPLOAD SUCCESSFUL!") }
            .addOnFailureListener { letEmKnow("USERNAME UPLOAD FAILED!") }


        val ref = Firebase.storage.reference
        viewModelScope.launch {
            try {
                val drawings = repository.drawings.first()
                coroutineScope {  // Ensures that all child coroutines complete
                    val tasks = drawings.map { drawing ->
                        async {
                            try {
                                ByteArrayOutputStream().use { baos ->
                                    drawing.bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos)
                                    val data = baos.toByteArray()
                                    val fileRef = ref.child("${_currentUser.value!!.uid}/${drawing.dPath.name}")
                                    fileRef.putBytes(data)
                                        .addOnFailureListener { e -> letEmKnow("PICUPLOAD Failed! $e") }
                                        .addOnSuccessListener { letEmKnow("PICUPLOAD Success!") }
                                        .await()  // Wait for the upload to complete
                                }
                            } catch (e: Exception) {
                                letEmKnow("Error during upload: ${e.message}")
                            }
                        }
                    }
                    tasks.awaitAll()
                }
                clear()
                Firebase.auth.signOut()
                usernameFlow.value = ""
                _currentUser.value = Firebase.auth.currentUser
                onSignOut()
            } catch (e: Exception) {
                letEmKnow("An error occurred: ${e.message}")
            }
        }


    }

}