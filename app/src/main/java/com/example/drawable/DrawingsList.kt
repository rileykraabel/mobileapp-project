package com.example.drawable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.drawable.databinding.FragmentDrawingsListBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DrawingsList : Fragment() {
    private lateinit var binding: FragmentDrawingsListBinding
    private var currentCount: Int = 0
   private val dateFormat = SimpleDateFormat("dd.LLL.yyyy HH:mm:ss aaa z", Locale.getDefault())
    var MMMddFormat = SimpleDateFormat("MMM dd", Locale.US) // Aug 31
    var hhmmampmFormat = SimpleDateFormat("hh:mm a", Locale.US) // 01:55 PM
    var yearFormat = SimpleDateFormat("yyyy", Locale.US) // 01:55 PM
    private var user: String? = null


    /**
     * Creates the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawingsListBinding.inflate(layoutInflater)

        val myViewModel: DrawableViewModel by activityViewModels {
            val application = requireActivity().application as DrawableApplication
            DrawableViewModel.Factory(application.drawingRepository)
        }

        val onClicked: (DrawingPath) -> Unit = { dpath ->
            myViewModel.setCurrBitmap(dpath)
            findNavController().navigate(
                R.id.action_drawingsList_to_drawingCanvas,
                Bundle().apply {
                    putString("Title", dpath.name)
                }
            )
        }

        val onDelete: (DrawingPath) -> Unit = { dPath ->
            myViewModel.removeDrawing(dPath)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myViewModel.count.collect { countValue ->
                currentCount = countValue
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myViewModel.usernameFlow.collect { updatedUsername ->
                user = updatedUsername
            }
        }

        binding.composeView1.setContent {
            val drawings by myViewModel.drawings.collectAsState(initial = emptyList())
            DrawingsListContent(drawings, onClicked, onDelete)
        }

        return binding.root
    }

    /**
     * Attaches listeners and restores saved items such as the list of drawings
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addDrawing.setOnClickListener {
            findNavController().navigate(R.id.action_drawingsList_to_drawingCanvas, Bundle().apply {
                putString("New", "Drawing " + (currentCount + 1))
            })
        }
       binding.addUser.setOnClickListener {
           findNavController().navigate(R.id.action_drawingsList_to_drawingLoginNRegister)
        }

        if(user !=  ""){
            val text = "$user's Drawing Gallery"
            binding.title.text = text
        }else{
            val text = "Drawing Gallery"
            binding.title.text = text
        }
    }

    /**
     *  Composable function used to draw the list
     *  @param drawings The list of drawings to draw
     *  @param onClick A callback passed to the list item
     *  @param onDelete  A callback passed to the list item
     */
    @Composable
    fun DrawingsListContent(
        drawings: List<Drawing>,
        onClick: (DrawingPath) -> Unit,
        onDelete: (DrawingPath) -> Unit
    ) {
        Column()
        {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            {
                items(items = drawings) { drawing ->
                    DrawingListItem(
                        drawing,
                        onClick = { onClick(drawing.dPath) },
                        onDelete = { onDelete(drawing.dPath) })
                }
            }
        }
    }

    /**
     * Composable function used to draw the list item
     *  @param drawing The drawing to represent as a list item
     *  @param onClick Handles what happens when the item is clicked
     *  @param onDelete Handles what happens when the item is deleted
     */
    @Composable
    fun DrawingListItem(
        drawing: Drawing,
        onClick: () -> Unit,
        onDelete: () -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }
        ElevatedCard(
            onClick = { onClick() },
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth() // This makes the width fill the maximum available space
                .height(dimensionResource(id = R.dimen.card_item_height)),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        )
        {
            Row(
                modifier = Modifier.padding(all = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add drawing preview
                Image(
                    bitmap = drawing.bitmap.asImageBitmap(),
                    contentDescription = "Drawing Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(50.dp)
                        .border(border = BorderStroke(.5.dp, Color.LightGray))
                )

                //Add horizontal spacer between drawing preview and title column
                Spacer(modifier = Modifier.width(15.dp))
                //Add column for title and the modification date
                Column {
                    // Add drawing title
                    Text(
                        text = drawing.dPath.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    // Add a vertical space between the drawing title and the modified date
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        //text = dateFormat.format(drawing.dPath.modDate),
                        text = hhmmampmFormat.format(drawing.dPath.modDate) + "  " + MMMddFormat.format(drawing.dPath.modDate) + ", " + yearFormat.format(drawing.dPath.modDate) ,
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 20.sp
                        )
                    )
                }
                // Add container for the more options button
                Box(
                    contentAlignment = Alignment.TopEnd, // Aligns the IconButton to the end (right)
                    modifier = Modifier
                        .fillMaxWidth() // Ensures the Box takes up the full width
                        .padding(end = 4.dp),

                ) {
                    FloatingActionButton(
                        onClick = { showMenu = !showMenu },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = Color.White
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.more_options_default),
                            contentDescription = "More Options",
                            //tint = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete",
                                    fontSize = 18.sp
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.trash_default), // Use the Material Icons Delete icon
                                    contentDescription = "Delete"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
