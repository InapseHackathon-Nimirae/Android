package kr.puze.nimire

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.SuppressLint
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.annotation.TargetApi
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.media.ExifInterface
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.Utils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.actionbar_main.*
import kr.puze.nimire.Adapter.AccountRecyclerViewAdapter
import kr.puze.nimire.Data.Account
import kr.puze.nimire.Data.All
import kr.puze.nimire.Data.Kcal
import kr.puze.nimire.Server.RetrofitService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnChartGestureListener {
    private val CAMERA_CODE = 111
    private val WRITE_CODE = 222
    private val READ_CODE = 333
    private val GALLERY_CODE = 444
    private val REQUEST_PERMISSION_CODE = 555

    companion object {
        lateinit var token: String
        lateinit var retrofitService: RetrofitService
        lateinit var call: Call<All>
        @SuppressLint("StaticFieldLeak")
        lateinit var builder: AlertDialog.Builder
        lateinit var photoUri: Uri
        lateinit var mImageCaptureName: String
        lateinit var currentPhotoPath: String
        lateinit var imgFile: File
    }

    var kcals: ArrayList<Kcal> = ArrayList<Kcal>()
    var accounts: ArrayList<Account> = ArrayList<Account>()
    var all: All = All(accounts, kcals)
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        val intent: Intent = intent
        token = "1374977822632346"

        retrofitSetting()

        actionbar_img.setOnClickListener {
            var charIntent = Intent(this@MainActivity, CharActivity::class.java)
            if(count == 1){
                charIntent.putExtra("gif", "http://172.20.10.4:3000/static/large.gif")
                charIntent.putExtra("text", "이대로라면 당신은 뚱이가 될거에요!")
            }else{
                charIntent.putExtra("gif", "http://172.20.10.4:3000/static/medium.gif")
                charIntent.putExtra("text", "당신은 보통 중의 보통! 보통이군요!")
            }
            startActivity(charIntent)
        }

        kcals.add(Kcal("2018.10.13", "30"))
        kcals.add(Kcal("2018.10.13", "60"))

        accounts.add(Account("2018.10.13", "스테이크", "35,000"))
        accounts.add(Account("2018.10.13", "핫바", "3,000"))

        all.account = accounts
        all.kcal = kcals

        val adapter = AccountRecyclerViewAdapter(accounts, this)

        recycler_account.adapter = adapter
        recycler_account.adapter.notifyDataSetChanged()

        call()
        setData(100,45f)
        adapter.itemClick = object : AccountRecyclerViewAdapter.ItemClick {
            override fun onItemClick(view: View?, position: Int) {
                val item = accounts[position]
                Toast.makeText(this@MainActivity, "날짜 : " + item.date + "\n음식 : " + item.title + "\n가격 : " + item.price, Toast.LENGTH_SHORT).show()
            }
        }
        image_add.setOnClickListener {
            checkPermission()
        }

        chart.onChartGestureListener = this
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

//        var llXAxis = LimitLine(10f, "Index 10")
//        llXAxis.lineWidth = 4f
//        llXAxis.enableDashedLine(10f, 10f, 0f)
//        llXAxis.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
//        llXAxis.textSize = 10f
//
//        var xAxis: XAxis = chart.getXAxis()
//        xAxis.enableGridDashedLine(10f, 10f, 0f)
//
//        var ll1 = LimitLine(150f, "Upper Limit")
//        ll1.lineWidth = 4f
//        ll1.enableDashedLine(10f, 10f, 0f)
//        ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
//        ll1.textSize = 10f
//
//        var ll2 = LimitLine(-30f, "Lower Limit")
//        ll2.lineWidth = 4f
//        ll2.enableDashedLine(10f, 10f, 0f)
//        ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
//        ll2.textSize = 10f
//
//        var leftAxis: YAxis = chart.axisLeft;
//        leftAxis.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1)
//        leftAxis.addLimitLine(ll2)
//        leftAxis.axisMaximum = 200f
//        leftAxis.axisMinimum = -50f
//        leftAxis.enableGridDashedLine(10f, 10f, 0f)
//        leftAxis.setDrawZeroLine(false)
//
//        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true)
//
//        chart.axisRight.isEnabled = false

        setDataKcal(45, 100)

        chart.animateX(2500)
//        var l: Legend = chart.legend
//
//        // modify the legend ...
//        l.form = Legend.LegendForm.LINE

        graph_kcal.setOnClickListener {
            setDataKcal(45, 100)
        }
        graph_money.setOnClickListener {
            setDataMoney(45, 100)
        }

    }

    private fun setDataKcal(count: Int, range: Int) {
        graph_kcal.setBackgroundResource(R.drawable.kcal_button_push)
        graph_money.setBackgroundResource(R.drawable.money_button)
        setData(100,45f)
    }

    private fun setDataMoney(count: Int, range: Int) {
        graph_money.setBackgroundResource(R.drawable.money_button_push)
        graph_kcal.setBackgroundResource(R.drawable.kcal_button)
        setData(100,45f)
    }

    private fun setData(count: Int, range: Float) {

        var values: ArrayList<Entry> = ArrayList<Entry>()


        for (i in 0..count) {
            var value: Double = (Math.random() * range) + 3
            values.add(Entry(i.toFloat(), value.toFloat(), resources.getDrawable(R.drawable.ic_add_black_24dp)))
        }

        var set1: LineDataSet

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            val fdata = FloatArray(2)
            fdata.set(0, 10f)
            fdata.set(1, 5f)
            set1.formLineDashEffect = DashPathEffect(fdata, 0f)
            set1.setFormSize(15f)

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                var drawable: Drawable = ContextCompat.getDrawable(applicationContext, R.drawable.fade_red)!!
                set1.fillDrawable = drawable;
            } else {
                set1.setFillColor(Color.BLACK);
            }

            var dataSets: ArrayList<ILineDataSet> = ArrayList<ILineDataSet>()
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            var data: LineData = LineData(dataSets)

            // set data
            chart.setData(data)
        }
    }

    private fun call() {
        call = retrofitService.call(token)
        call.enqueue(object : Callback<All> {
            override fun onResponse(call: Call<All>?, response: Response<All>?) {
                Log.d("server_call", "onResponse")
                if (response != null) {
                    if (response.code() == 200) {
                        Toast.makeText(this@MainActivity, "데이터 불러오기 성공", Toast.LENGTH_SHORT).show()
                        if (response.body() != null) {
                            accounts = response.body()!!.account
                            kcals = response.body()!!.kcal
                            all = All(accounts, kcals)
                            setData(kcals.size, 30f)
                            recycler_account.adapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "code is ", response.code()).show()
                        Log.d("CODE:", response.code().toString())
                    }
                }
            }

            override fun onFailure(call: Call<All>?, t: Throwable?) {
                Toast.makeText(this@MainActivity, "서버에러", Toast.LENGTH_SHORT).show()
                if (t != null) {
                    Log.e("Error", t.message)
                }
            }
        })
    }

    private fun upLoad() {
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("multipart/png"), imgFile)

        val netBooktoken: RequestBody = RequestBody.create(okhttp3.MultipartBody.FORM, token)
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("file", imgFile.name, requestFile)

        call = retrofitService.upload(netBooktoken, body)
        call.enqueue(object : Callback<All> {
            override fun onResponse(call: Call<All>?, response: Response<All>?) {
                Log.d("server_upload", "onResponse")
                Log.d("server_token", token)
                if (response != null) {
                    if (response.code() == 200) {
                        Toast.makeText(this@MainActivity, "업로드 성공", Toast.LENGTH_SHORT).show()
                        if (response.body() != null) {
                            accounts = response.body()!!.account
                            kcals = response.body()!!.kcal
                            all = All(accounts, kcals)
                            recycler_account.adapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "code is ", response.code()).show()
                        Log.d("CODE:", response.code().toString())
                    }
                }
            }

            override fun onFailure(call: Call<All>?, t: Throwable?) {
                Toast.makeText(this@MainActivity, "서버에러", Toast.LENGTH_SHORT).show()
                if (t != null) {
                    Log.e("Error", t.message)
                }
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // 권한을 활성화 해주기 위한 설명
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "카메라로 찍어 업로드한 사진을 갤러리에 저장하기 위해 권한을 허용해주세요..", Toast.LENGTH_SHORT).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "업로드할 사진을 갤러리에서 불러오기 위해 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "업로드할 사진을 카메라에서 촬영하기 위해 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }

            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA), REQUEST_PERMISSION_CODE)

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        } else {
            // 모든 권한 항상 허용
            selectUpload()
        }
    }

    //사진을 업로드할 방법 선택
    private fun selectUpload() {
        builder = AlertDialog.Builder(this)

        builder.setTitle("사진 업로드")
        builder.setMessage("사진을 업로드할 방법을 선택해주세요.")
        builder.setNeutralButton("갤러리") { _, _ -> selectGallery() }
        builder.setPositiveButton("카메라") { _, _ -> selectCamera() }
        builder.setNegativeButton("취소", null)
        builder.setCancelable(true)
        builder.create()
        builder.show()
    }

    //업로드할 사진을 카메라로 촬영
    private fun selectCamera() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, packageName, photoFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, CAMERA_CODE)
                }
            }
        }
    }

    //파일 생성
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/path/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mImageCaptureName = timeStamp + ".png"

        val storageDir = File(Environment.getExternalStorageDirectory().absoluteFile.toString() + "/path/" + mImageCaptureName)
        currentPhotoPath = storageDir.absolutePath

        return storageDir
    }

    //사진 갤러리에 저장
    private fun savePhotoIntoGallery(currentPhotoPath: String) {
        val f = File(currentPhotoPath)
        val contentUri = Uri.fromFile(f)
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = contentUri

        this.sendBroadcast(mediaScanIntent)
    }

    //카메라로 찍은 사진 적용
    private fun getPictureForPhoto() {
        imgFile = File(currentPhotoPath)
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
        savePhotoIntoGallery(currentPhotoPath)
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(currentPhotoPath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val exifOrientation: Int
        val exifDegree: Int

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifDegree = exifOrientationToDegrees(exifOrientation)
        } else {
            exifDegree = 0
        }
//        image_add.setImageBitmap(rotate(bitmap, exifDegree.toFloat()))//이미지 뷰에 비트맵 넣기
        upLoad()
    }

    //업로드할 사진을 갤러리에서 선택
    private fun selectGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_CODE)
    }

    //선택한 사진에 대한 데이터 처리
    private fun sendPicture(imgUri: Uri) {
        var imagePath: String = getRealPathFromURI(imgUri) // path 경로
        imgFile = File(imagePath)

        var exif = ExifInterface(imagePath)

        var exifOrientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var exifDegree: Int = exifOrientationToDegrees(exifOrientation)

        var bitmap: Bitmap = BitmapFactory.decodeFile(imagePath)//경로를 통해 비트맵으로 전환
//        image_add.setImageBitmap(rotate(bitmap, exifDegree.toFloat()))//이미지 뷰에 비트맵 넣기
        upLoad()
    }

    //사진이 저장된 절대 경로 구하기
    @SuppressLint("Recycle")
    private fun getRealPathFromURI(contentUri: Uri): String {
        var column_index = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }

        return cursor.getString(column_index)
    }

    //사진의 회전값 구하기
    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    //사진을 정방향대로 회전
    private fun rotate(src: Bitmap, degree: Float): Bitmap {

        // Matrix 객체 생성
        val matrix = Matrix()
        // 회전 각도 셋팅
        matrix.postRotate(degree)
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.width,
                src.height, matrix, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    Toast.makeText(applicationContext, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
            //TODO Spinner 에서 사진첩 고정시켜줘야함
                GALLERY_CODE -> sendPicture(data!!.data) //갤러리에서 가져오기
                CAMERA_CODE -> getPictureForPhoto() //카메라에서 가져오기
            }
        }
    }

    private fun retrofitSetting() {
        val gson: Gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://172.20.10.4:3000")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }


    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
        Log.i("Gesture", "START, x: " + me.x + ", y: " + me.y)
    }

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture)

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            chart.highlightValues(null) // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
