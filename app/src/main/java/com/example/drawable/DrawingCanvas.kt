package com.example.drawable

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.drawable.databinding.FragmentDrawingCanvasBinding
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class DrawingCanvas : Fragment() {

    data class PaintedPath(val path: Path, val color: Int, val width: Float, val shape: Paint.Cap)
    data class Circles(val x: Double, val y: Double, val radius: Float, val paint: Paint)
    private var _binding: FragmentDrawingCanvasBinding? = null
    private val binding by lazy { _binding!! }
    private var currColor: Int? = null
    private var title: String? = null
    private var state: String? = null
    private var canvasView: CanvasView? = null
    private lateinit var gestureDetector: GestureDetector
    private val myViewModel: DrawableViewModel by activityViewModels()

    //bitmap drawing vars
    private var myBitmap: Bitmap? = null
    private var isDrag = false
    private var isSquare = false
    private var isFill = false
    private var isErase = false
    private var isSpray = false
    private var offsetX: Float? = null
    private var offsetY: Float? = null
    private val medWidth: Float = 10F
    private val thinWidth: Float = 2F
    private val thickWidth: Float = 25F
    private val eraseWidth: Float = 15F
    private var currPenSize: Float = medWidth
    private var currPenShape: Paint.Cap = Paint.Cap.ROUND
    private var paintbrush = Paint()
    private var pathList = ArrayList<PaintedPath>()
    private var circles = ArrayList<Circles>()
    private var bitmapWidth: Int? = null
    private var bitmapHeight: Int? = null
    private var viewWidth: Float? = null
    private var viewHeight: Float? = null
    private var width = 8F
    private var currentPath = Path()
    private var defaultIconColor = 0xffff00
    private var pressedIconColor = 0x325AFF


    /**
     * Creates the view
     *  @param inflater
     *  @param container
     *  @param savedInstanceState
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDrawingCanvasBinding.inflate(layoutInflater)
        return binding.root
    }

    /**
     * Attaches listeners and restores saved items such as the bitmap or color
     *  @param view
     *  @param savedInstanceState
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // displays color picker
        binding.pallete.setOnClickListener {
            loadColorPicker()
        }

        //Evaluates the drawing state and sets the title and other vars accordingly
        state = requireArguments().getString("New")
        if (state != null) {
            title = state
            val bitmap = createNewBitmap()
            myViewModel.updateBitmap(bitmap)
            currColor = Color.BLACK
            myViewModel.updateColor(currColor!!)
        } else {
            title = requireArguments().getString("Title")
        }

        state = requireArguments().getString("Existing")

        myBitmap = myViewModel.currBitmap.value
        currColor = myViewModel.currColor.value
        binding.Title.setText(title)

        myViewModel.currColor.observe(viewLifecycleOwner) { color ->
            currColor = color
            paintbrush.color = color
        }

        //observe changes on bitmap
        myViewModel.currBitmap.observe(viewLifecycleOwner) { bitmap ->
            myBitmap = bitmap // Assign it to fragment's bitmap variable
            binding.canvas.setBitmap(bitmap)
        }

        // displays pen size / shape popup
        binding.paintBrush.setOnClickListener {
            isSpray = false
            isFill = false
            isErase = false
            showPopUp()
        }

        canvasView = binding.canvas
        bitmapHeight = myBitmap!!.height
        bitmapWidth = myBitmap!!.width

        //Handles double tap on title
        gestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    binding.Title.isFocusableInTouchMode = true
                    binding.Title.requestFocus()
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.Title, InputMethodManager.SHOW_IMPLICIT)
                    return true
                }
            })
        binding.Title.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        //Handles when user is done editing title
        binding.Title.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.Title.isFocusable = false
                true
            } else {
                false
            }
        }

        //Moves back to list fragment and saves drawing
        binding.backButton.setOnClickListener { onBackClicked() }
        //Handles drawing on canvas
        canvasView!!.setOnTouchListener { _, event -> onCanvasTouch(event) }

        binding.paintBucket.setOnClickListener {
            isSpray = false
            isFill = true
            isErase = false
        }
        binding.eraser.setOnClickListener {
            isSpray = false
            isErase = true
            isFill = false
        }

        binding.spraycan.setOnClickListener {
            isSpray = true
            isErase = false
            isFill = false
        }

        binding.blur?.setOnClickListener {
            myViewModel.brightenImage()
            pathList.clear()
        }

        binding.inverter?.setOnClickListener {
            myViewModel.invertColors()
            pathList.clear()
        }

        //Initializes things to draw
        initBrush()
        initVars()
    }

    /**
     * Initializes brush properties
     */
    private fun initBrush() {
        paintbrush.isAntiAlias = true
        paintbrush.color = currColor!!
        paintbrush.strokeWidth = width
        paintbrush.style = Paint.Style.STROKE
        paintbrush.strokeJoin = Paint.Join.ROUND
        paintbrush.strokeCap = Paint.Cap.ROUND
    }

    /**
     * Initializes variables for drawing
     */
    private fun initVars() {
        viewWidth = canvasView!!.width.toFloat()
        viewHeight = canvasView!!.height.toFloat()
        val swidth = viewWidth!! / bitmapWidth!!
        val sheight = viewHeight!! / bitmapHeight!!
        offsetX = (viewWidth!! - bitmapWidth!! * swidth) / 2
        offsetY = (viewHeight!! - bitmapHeight!! * sheight) / 2
    }

    /**
     * Handles touch events on the canvas
     *  @param event
     */
    private fun onCanvasTouch(event: MotionEvent): Boolean {
        val (bX, bY) = translatecoords(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isFill) {
                    val targetColor = myBitmap!!.getPixel(bX.toInt(), bY.toInt())
                    applyFloodFill(bX.toInt(), bY.toInt(), targetColor, paintbrush.color)
                } else if (isSpray) {
                    getCircles(bX, bY, paintbrush.color, paintbrush.strokeWidth * 4, 1F)
                } else {
                    currentPath.moveTo(bX, bY)
                    isDrag = false
                    drawOnBitmap()
                }

            }

            MotionEvent.ACTION_MOVE -> {
                isDrag = true // Set flag to indicate dragging
                currentPath.lineTo(bX, bY)
                // Add to pathList only when significant movement has occurred to avoid duplicate paths
                if (!pathList.contains(
                        PaintedPath(
                            currentPath,
                            paintbrush.color,
                            paintbrush.strokeWidth,
                            paintbrush.strokeCap
                        )
                    )
                ) {
                    if (isErase) {
                        pathList.add(
                            PaintedPath(
                                Path(currentPath),
                                Color.WHITE,
                                eraseWidth,
                                paintbrush.strokeCap
                            )
                        )
                    }else if(isSpray){
                        getCircles(bX, bY, paintbrush.color, paintbrush.strokeWidth * 4, 1F)
                    } else {
                        pathList.add(
                            PaintedPath(
                                Path(currentPath),
                                paintbrush.color,
                                paintbrush.strokeWidth,
                                paintbrush.strokeCap
                            )
                        )
                    }
                }
                drawOnBitmap()
            }

            MotionEvent.ACTION_UP -> {
                if (isDrag) {
                    if (isErase) {
                        pathList.add(
                            PaintedPath(
                                Path(currentPath),
                                Color.WHITE,
                                eraseWidth,
                                paintbrush.strokeCap
                            )
                        )
                    } else if(isSpray){
                        getCircles(bX, bY, paintbrush.color, paintbrush.strokeWidth * 4, 1F)
                    }else {
                        pathList.add(
                            PaintedPath(
                                Path(currentPath),
                                paintbrush.color,
                                paintbrush.strokeWidth,
                                paintbrush.strokeCap
                            )
                        )
                    }
                    currentPath.reset()
                } else {
                    drawDot(bX, bY)
                }
                drawOnBitmap()
            }
        }
        return true
    }

    /**
     * Applies fill to the canvas
     *  @param startX
     *  @param startY
     *  @param targetColor
     *  @param replacementColor
     */
    private fun applyFloodFill(startX: Int, startY: Int, targetColor: Int, replacementColor: Int) {
        val queue = LinkedList<Pair<Int, Int>>()
        queue.add(Pair(startX, startY))

        while (queue.isNotEmpty()) {
            val (x, y) = queue.poll()!!
            //if (x < 0 || x >= myBitmap!!.width || y < 0 || y >= myBitmap!!.height) continue
            if(!(x >=0 && x <= myBitmap!!.width && y >=0 && y <= myBitmap!!.height)) continue
            if (myBitmap!!.getPixel(x, y) != targetColor) continue
            if (myBitmap!!.getPixel(x, y) == replacementColor) continue


            myBitmap!!.setPixel(x, y, replacementColor)
            queue.add(Pair(x + 1, y))
            queue.add(Pair(x - 1, y))
            queue.add(Pair(x, y + 1))
            queue.add(Pair(x, y - 1))
        }
    }

    /**
     * Creates a new white bitmap
     */
    private fun createNewBitmap(): Bitmap {
        return Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
    }

    /**
     * Handles drawing on the bitmap
     */
    private fun drawOnBitmap() {
        val canvas = Canvas(myBitmap!!)
        val tempBrush = Paint()
        tempBrush.style = Paint.Style.STROKE
        for (path in pathList) {
            tempBrush.color = path.color
            tempBrush.strokeWidth = path.width
            tempBrush.strokeCap = path.shape
            canvas.drawPath(path.path, tempBrush)
        }
        for (c in circles){
            canvas.drawCircle(c.x.toFloat(), c.y.toFloat(), c.radius, c.paint)
        }

        myViewModel.updateBitmap(myBitmap!!)
        updateCanvasView()
    }

    /**
     * Allows the user to draw dots
     * @param x the x coordinate
     * @param y the y coordinate
     */
    private fun drawDot(x: Float, y: Float) {
        val dotPath = Path()
        val halfStrokeWidth = paintbrush.strokeWidth / 2

        if (isSquare) {
            dotPath.moveTo(x - halfStrokeWidth, y - halfStrokeWidth) // Top-left
            dotPath.lineTo(x + halfStrokeWidth, y - halfStrokeWidth) // Top-right
            dotPath.lineTo(x + halfStrokeWidth, y + halfStrokeWidth) // Bottom-right
            dotPath.lineTo(x - halfStrokeWidth, y + halfStrokeWidth) // Bottom-left
            dotPath.close()
        } else {
            dotPath.addCircle(x, y, halfStrokeWidth, Path.Direction.CW)
        }
        pathList.add(
            PaintedPath(
                dotPath,
                paintbrush.color,
                paintbrush.strokeWidth,
                paintbrush.strokeCap
            )
        )
    }

    /**
     * Updates the canvas with the bitmap
     */
    private fun updateCanvasView() {
        canvasView!!.setBitmap(myBitmap!!)
    }

    /**
     * Translates view coordinates to bitmap coordinates
     * @param touchX x coordinate in the view
     * @param touchY y coordinate in the view
     */
    private fun translatecoords(touchX: Float, touchY: Float): Pair<Float, Float> {
        val bitmapX = touchX - offsetX!!
        val bitmapY = touchY - offsetY!!
        return Pair(bitmapX, bitmapY)
    }

    /**
     * Displays the color picker
     */
    private fun loadColorPicker() {
        AmbilWarnaDialog(requireActivity(), currColor!!,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    myViewModel.updateColor(color)
                }
            }).show()
    }

    /**
     * Displays the pop up for changing sizes and shape
     */
    private fun showPopUp() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val thinPen: ImageButton = dialog.findViewById<ImageButton>(R.id.thinPen)
        val medPen: ImageButton = dialog.findViewById<ImageButton>(R.id.medPen)
        val thickPen: ImageButton = dialog.findViewById<ImageButton>(R.id.thickPen)

        val trianglePen: ImageButton = dialog.findViewById<ImageButton>(R.id.trianglePen)
        val squarePen: ImageButton = dialog.findViewById<ImageButton>(R.id.squarePen)
        val roundPen: ImageButton = dialog.findViewById<ImageButton>(R.id.roundPen)

        // size listeners //
        thinPen.setOnClickListener {
            currPenSize = thinWidth
            setPenSize(currPenSize)
            dialog.hide()
        }
        medPen.setOnClickListener {
            currPenSize = medWidth
            setPenSize(currPenSize)
            dialog.hide()
        }
        thickPen.setOnClickListener {
            currPenSize = thickWidth
            setPenSize(currPenSize)
            dialog.hide()
        }

        // shape listeners //
        trianglePen.setOnClickListener {
            currPenShape = Paint.Cap.BUTT
            setPenShape(currPenShape)
            dialog.hide()
        }
        squarePen.setOnClickListener {
            currPenShape = Paint.Cap.SQUARE
            isSquare = true
            setPenShape(currPenShape)
            dialog.hide()
        }
        roundPen.setOnClickListener {
            currPenShape = Paint.Cap.ROUND
            isSquare = false
            setPenShape(currPenShape)
            dialog.hide()
        }

        dialog.show()
    }

    /**
     * Sets the size of the pen
     * @param penSize the new pen size
     */
    private fun setPenSize(penSize: Float) {
        paintbrush.strokeWidth = penSize
        currPenSize = paintbrush.strokeWidth
        currentPath = Path()
    }

    /**
     * Sets the shape of the pen
     * @param penShape the new pen shape
     */
    private fun setPenShape(penShape: Paint.Cap) {
        paintbrush.strokeCap = penShape
        currPenShape = paintbrush.strokeCap
        currentPath = Path()
    }

    /**
     * Saves the drawing when back is clicked, then goes back to the list of drawings
     */
    private fun onBackClicked() {
        //If the user has drawn on the canvas
        if (pathList.size > 0) {
            var result: Boolean
            lifecycleScope.launch {
            //If the name already exists, the user should name the drawing something else
            if (myViewModel.checkForDrawing(binding.Title.text.toString())) {
                showAlertDialog()
            } else {
                val d = Drawing(
                    myBitmap!!,
                    DrawingPath(System.currentTimeMillis(), binding.Title.text.toString())
                )
                myViewModel.add(d)
                findNavController().popBackStack()
            }
        }
        }else{
            findNavController().popBackStack()
        }
    }

    private fun showAlertDialog() {
        if (context != null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("Let the User know!!");
            builder.setMessage("This name already exists.\n Pick another one :P")
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, _ -> // Code to execute when the OK button is clicked
                    dialog.dismiss()
                })
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            Toast.makeText(activity, "Context is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Used to draw paintcan effect
     *  @param centerX
     *  @param centerY
     *  @param brushColor
     *  @param brushWidth
     *  @param airBrushDensity
     *  @param spreadMultiplier
     *  @param dotSize
     */
    private fun getCircles(
        centerX: Float,
        centerY: Float,
        brushColor: Int,
        brushWidth: Float,
        airBrushDensity: Float,
        spreadMultiplier: Float = 1.7f, // Controls the spread of the dots 1.2
        dotSize: Float = 0.2f // Controls the size of the dots .2
    ) {
        val paint = Paint().apply {
            color = brushColor
            isAntiAlias = true
        }

        val radius = brushWidth / 2f * spreadMultiplier // Increased spread
        val rng = Random.Default

        val pixelCount: Int = if (brushWidth == 1F) {
            rng.nextInt(2) // Either 0 or 1, simulating a round() call
        } else {
            kotlin.math.ceil(Math.PI * radius * radius * airBrushDensity).toInt()
        }

        for (i in 0 until pixelCount) {
            val angle = rng.nextDouble() * 2 * Math.PI
            val distance = rng.nextDouble() * radius
            val x = centerX + distance * sin(angle).toFloat()
            val y = centerY + distance * cos(angle).toFloat()
            val alpha = rng.nextFloat() * 0.9f
            paint.alpha = (alpha * 255).toInt()
            circles.add(Circles(x, y, dotSize, paint)) // Using dotSize for the dot's radius
        }
    }

    /**
     * Destroys the view
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}