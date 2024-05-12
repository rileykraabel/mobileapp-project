package com.example.drawable


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import java.io.File


class DrawingLoginNRegister : Fragment() {

    private val myViewModel: DrawableViewModel by activityViewModels {
        val application = requireActivity().application as DrawableApplication
        DrawableViewModel.Factory(application.drawingRepository)
    }


    /**
     * Creates the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())

        view.apply {
            setContent {
                MainScreen(myViewModel)
            }
        }

        return view
    }

    /**
     * Main Composable that decides which screen to show based on a boolean flag.
     */
    @Composable
    fun MainScreen(viewModel: DrawableViewModel) {
        val state by viewModel.state.collectAsState()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        val modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )


        when (state) {
        // If user is logging in
            LoginState.NotLogged -> Login(modifier,  {myViewModel.startSignUp()}) {
                myViewModel.login()
            }
         //If user is signing up
            LoginState.SigningUp -> Register(modifier) {
                myViewModel.login()
            }
         //If user is signed in
            LoginState.LoggedIn -> UserPage(modifier) {
                myViewModel.signOut()
            }
        }
    }

    /**
     *
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Login(modifier: Modifier, onRegisterClicked: () -> Unit, onSignedIn: ()-> Unit) {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        val isLoggingIn = remember { mutableStateOf(false) }
        val context = LocalContext.current //Maybe pass this?

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { findNavController().popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .padding(innerPadding)

            ) {
                Logo()

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "Log in",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                EmailTextField(email) { email = it }
                Spacer(modifier = Modifier.height(20.dp))
                PasswordTextField(password) { password = it }
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            toaster(requireActivity(),"Username or password cannot be empty")
                        } else {
                            isLoggingIn.value = true
                            myViewModel.log_in(email, password, {toaster(requireActivity(), it)}, context, onSignedIn)// Proceed with login
                            isLoggingIn.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF6C80E8))
                ) {
                    Text("Login")
                }
                Spacer(modifier = Modifier.height(75.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ")
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onRegisterClicked() },
                        modifier = Modifier.background(Color.Transparent),
                        colors = ButtonDefaults.buttonColors(Color(0xFF6C80E8))
                    ) {
                        Text("Sign up")
                    }
                }
                if (isLoggingIn.value) {
                    Loading()
                }
            }
        }
    }

    /**
     *
     */
    private fun toaster(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     *
     */
    @Composable
    fun Loading() {
        Dialog(onDismissRequest = {}) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp) // You can adjust the size
                )
            }
        }
    }


    /**
     *
     */
    @Composable
    fun Logo() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(120.dp) // You can adjust the size as needed
            )
            Spacer(Modifier.width(2.dp)) // Adds some space between the icon and text

            Text(
                "Drawable", style = TextStyle(
                    color = Color.Black,
                    fontSize = 30.sp,

                    )
            )
        }
    }

    /**
     *
     */
    @Composable
    fun EmailTextField(email: String, onEmailChange: (String) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier
                    .width(350.dp)
                    .border(
                        border = BorderStroke(1.dp, Color(0xFFC1C7D6)),
                        shape = RoundedCornerShape(50.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email), // Load the drawable resource
                        contentDescription = "login",
                        modifier = Modifier.size(30.dp),
                        tint = Color(0xFFC1C7D6)
                    )
                }
            )
        }
    }

    /**
     *
     */
    @Composable
    fun PasswordTextField(password: String, onPasswordChange: (String) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier
                    .width(350.dp)
                    .border(
                        border = BorderStroke(1.dp, Color(0xFFC1C7D6)),
                        shape = RoundedCornerShape(50.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "login",
                        modifier = Modifier.size(30.dp),
                        tint = Color(0xFFC1C7D6)
                    )
                }
            )
        }
    }

    /**
     *
     */
    @Composable
    fun UsernameTextField(nickName: String, onNameChange: (String) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = nickName,
                onValueChange = onNameChange,
                label = { Text("Username") },
                modifier = Modifier
                    .width(350.dp)
                    .border(
                        border = BorderStroke(1.dp, Color(0xFFC1C7D6)),
                        shape = RoundedCornerShape(50.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "login",
                        modifier = Modifier.size(30.dp),
                        tint = Color(0xFFC1C7D6)
                    )
                }
            )
        }
    }

    /**
     *
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Register(modifier: Modifier, onSignUpClicked: () -> Unit) {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var username by rememberSaveable { mutableStateOf("") }
        val isRegistering = remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { findNavController().popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .padding(innerPadding)
            ) {

                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = "Sign Up",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )

                Spacer(modifier = Modifier.height(100.dp))
                UsernameTextField(username) { username = it }
                Spacer(modifier = Modifier.height(20.dp))
                EmailTextField(email) { email = it }
                Spacer(modifier = Modifier.height(25.dp))
                PasswordTextField(password) { password = it }
                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = {
                        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                            toaster(requireActivity(),"Username, Password, or Email cannot be empty")
                        } else {
                            isRegistering.value = true
                            myViewModel.register(username, email, password, { toaster(requireActivity(),it) }, onSignUpClicked)
                            isRegistering.value = false // Stop showing the progress indicator
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF6C80E8))
                ) {
                    Text("Sign up")
                }
                if (isRegistering.value) {
                    Loading()
                }
            }
        }
    }

    /**
     *
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UserPage(modifier: Modifier, onSignOutClicked: () -> Unit) {
        val currentUser by myViewModel.currentUser.collectAsState()
        val email = currentUser!!.email
        val username by myViewModel.usernameFlow.collectAsState()
        val isSigningOut = remember { mutableStateOf(false) }

        val drawing by myViewModel.drawings.collectAsState(initial = listOf()) //use to draw drawing in a grid, and get the number of drawings

        val unChanged = remember { mutableStateOf(false) }
        val emChanged = remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { findNavController().popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .padding(innerPadding)
            ) {
                Logo()

                val emailText = remember { mutableStateOf(email) }
                val usernameText = remember { mutableStateOf(username) }

                Spacer(modifier = Modifier.height(100.dp))

                UsernameTextField(usernameText.value ?: "") { // If usernameText.value is null, use empty string ""
                    usernameText.value = it
                    unChanged.value = true
                }
                Spacer(modifier = Modifier.height(20.dp))

                EmailTextField(emailText.value ?: "") { // If emailText.value is null, use empty string ""
                    emailText.value = it
                    emChanged.value = true
                }

                Spacer(modifier = Modifier.height(75.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Allows the user to change their username and email
                    Button(
                        onClick = {

                            if (emChanged.value) {
                                //call update
                                myViewModel.update_user(usernameText.value!!, emailText.value!!, unChanged.value, emChanged.value
                                ) { toaster(requireActivity(),it) }
                                emChanged.value = false
                            }

                            if (unChanged.value) {
                                // call update
                                myViewModel.update_user(usernameText.value!!, emailText.value!!, unChanged.value, emChanged.value
                                ) { toaster(requireActivity(),it) }
                                unChanged.value = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF6C80E8))
                    ) {
                        Text("Update User")
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                    Button(
                        onClick = {
                            isSigningOut.value = true
                            myViewModel.sign_out({ toaster(requireActivity(),it) }){
                                onSignOutClicked()
                            }
                            isSigningOut.value = false
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF6C80E8))
                    ) {
                        Text("Sign Out")
                    }
                }
                Spacer(modifier = Modifier.height(25.dp))

                Images_Grid(drawing)

                if (isSigningOut.value) {
                    Loading()
                }
            }
        }
    }

    @Composable
    fun Images_Grid(drawings: List<Drawing>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(drawings) { drawing ->
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        text = drawing.dPath.name, // Assuming each drawing has a title property
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Image(
                        bitmap = drawing.bitmap.asImageBitmap(),
                        contentDescription = "Displayed image",
                        modifier = Modifier.aspectRatio(1f),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }

    @Preview(showBackground = true, widthDp = 412, heightDp = 892)
    @Composable
    fun LoginPreview() {
        Login(Modifier, {}) {}
    }

    @Preview(showBackground = true, widthDp = 412, heightDp = 892)
    @Composable
    fun RegisterPreview() {
        Register(Modifier) {}
    }

    @Preview(showBackground = true, widthDp = 412, heightDp = 892)
    @Composable
    fun UserPagePreview() {
        UserPage(Modifier) {}
    }
}