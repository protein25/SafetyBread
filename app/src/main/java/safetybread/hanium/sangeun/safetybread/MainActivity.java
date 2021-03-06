package safetybread.hanium.sangeun.safetybread;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends PermissionActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 10;

    public static Realm realm;
    public static RealmResults<ServiceArea> results;

    private BottomNavigationView navigationView;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    private int mPariedDeviceCount = 0;
    private Set<BluetoothDevice> mDevices;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mRemoteDevie;

    private BluetoothSocket mSocket = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private String mStrDelimiter = "\n";
    private char mCharDelimiter = '\n';

    private Thread mWorkerThread = null;
    private byte[] readBuffer;
    private int readBufferPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() : starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingPermissions();

        Realm.init(getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(configuration);
        results = realm.where(ServiceArea.class).findAll();

        initData();

//        if (results == null) {
//            initData();
//        } else {
//            Log.d("getData", "Realm already have ServiceArea data.");
//        }

        navigationView = findViewById(R.id.design_navigation_view);
        navigationView.setOnNavigationItemSelectedListener(navigationListener);

        pager = findViewById(R.id.pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new CarbonFragment());
        pagerAdapter.addFragment(new ServiceAreaFragment());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(pageListener);
    }

    public void initData(){
        JSONArray array = getJson(getResources().openRawResource(R.raw.service_area_api));
        try {
            if (array.length() == 0) {
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                if (object.isNull("위도")) {
                    continue;
                }
                realm.beginTransaction();
                ServiceArea temp = realm.createObject(ServiceArea.class);
                if (!object.isNull("도로종류"))
                    temp.routeType = object.getString("도로종류");
                if (!object.isNull("매점유무"))
                    temp.hasStore = object.getString("매점유무").equals("Y");
                if (!object.isNull("휴게소운영시작시각"))
                    temp.startServiceAt = object.getString("휴게소운영시작시각");
                if (!object.isNull("휴게소운영종료시각"))
                    temp.endServiceAt = object.getString("휴게소운영종료시각");
                if (!object.isNull("전기차충전소유무"))
                    temp.hasElectricCarCharge = object.getString("전기차충전소유무").equals("Y");
                if (!object.isNull("휴게소대표음식명"))
                    temp.signatureFood = object.getString("휴게소대표음식명");
                if (!object.isNull("수유실유무"))
                    temp.hasFeedingRoom = object.getString("수유실유무").equals("Y");
                if (!object.isNull("휴게소명"))
                    temp.areaName = object.getString("휴게소명");
                if (!object.isNull("화장실유무"))
                    temp.hasToilet = object.getString("화장실유무").equals("Y");
                if (!object.isNull("약국유무"))
                    temp.hasPharmacy = object.getString("약국유무").equals("Y");
                if (!object.isNull("경정비가능여부"))
                    temp.hasMaintenance = object.getString("경정비가능여부").equals("Y");
                if (!object.isNull("음식점유무"))
                    temp.hasCafeteria = object.getString("음식점유무").equals("Y");
                if (!object.isNull("위도"))
                    temp.latitude = object.getDouble("위도");
                if (!object.isNull("경도"))
                    temp.longitude = object.getDouble("경도");
                if (!object.isNull("LPG충전소유무"))
                    temp.hasLPGCharge = object.getString("LPG충전소유무").equals("Y");
                if (!object.isNull("주유소유무"))
                    temp.hasGasolineCharge = object.getString("주유소유무").equals("Y");
                if (!object.isNull("도로노선명"))
                    temp.routeName = object.getString("도로노선명");
                if (!object.isNull("휴게소전화번호"))
                    temp.tel = object.getString("휴게소전화번호");
                if (!object.isNull("쉼터유무"))
                    temp.hasRestPlace = object.getString("쉼터유무").equals("Y");

                realm.commitTransaction();
                Log.d("object", object.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //파일 데이터 파싱 메서드
    static public JSONArray getJson(InputStream is) {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonString = writer.toString();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public void settingPermissions() {
        addPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        addPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        checkAndRequestPermission();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.carbon_concentrate:
                    pager.setCurrentItem(0);
                    return true;
                case R.id.serviceArea:
                    pager.setCurrentItem(1);
                    return true;
            }
            return false;
        }
    };

    private ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < pagerAdapter.getCount(); i++) {
                pagerAdapter.getItem(i).setHasOptionsMenu(false);
                if (i == position) {
                }
                invalidateOptionsMenu();
            }
            switch (position) {
                case 0:
                    navigationView.setSelectedItemId(R.id.carbon_concentrate);
                    break;
                case 1:
                    navigationView.setSelectedItemId(R.id.serviceArea);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    void checkBluetooth() {
        /**
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {  // 블루투스 미지원
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();  // 앱종료
        } else { // 블루투스 지원
            /** isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
             *               true : 지원 ,  false : 미지원
             */
            if (!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
                /**
                 startActivityForResult 함수 호출후 다이얼로그가 나타남
                 "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
                 "아니오" 를 선택하면 비활성화 상태를 유지 한다.
                 선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
                 */
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else // 블루투스 지원하며 활성 상태인 경우.
                selectDevice();
        }
    }

    void selectDevice() {
        // 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPariedDeviceCount = mDevices.size();

        if (mPariedDeviceCount == 0) { // 페어링된 장치가 없는 경우.
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            finish(); // App 종료.
        }
        // 페어링된 장치가 있는 경우.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        // 각 디바이스는 이름과(서로 다른) 주소를 가진다. 페어링 된 디바이스들을 표시한다.
        List<String> listItems = new ArrayList<String>();
        for (BluetoothDevice device : mDevices) {
            // device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
            listItems.add(device.getName());
        }
        listItems.add("취소");  // 취소 항목 추가.


        // CharSequence : 변경 가능한 문자열.
        // toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        // toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if (item == mPariedDeviceCount) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
                    Toast.makeText(getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    finish();
                } else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.
                    connectToSelectedDevice(items[item].toString());
                }
            }

        });

        builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
        AlertDialog alert = builder.create();
        alert.show();
    }

    //  connectToSelectedDevice() : 원격 장치와 연결하는 과정을 나타냄.
    //   실제 데이터 송수신을 위해서는 소켓으로부터 입출력 스트림을 얻고 입출력 스트림을 이용하여 이루어 진다.
    void connectToSelectedDevice(String selectedDeviceName) {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        mRemoteDevie = getDeviceFromBondedList(selectedDeviceName);
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성, RFCOMM 채널을 통한 연결.
            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와
            //                                           통신할 수 있는 소켓을 생성함.
            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는
            //  BluetoothSocket 오브젝트를 리턴함.
            mSocket = mRemoteDevie.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.

            // 데이터 송수신을 위한 스트림 얻기.
            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
            // 1. 데이터를 보내기 위한 OutputStrem
            // 2. 데이터를 받기 위한 InputStream
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // 데이터 수신 준비.
            beginListenForData();

        } catch (Exception e) { // 블루투스 연결 중 오류 발생
            Toast.makeText(getApplicationContext(),
                    "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();  // App 종료
        }
    }

    BluetoothDevice getDeviceFromBondedList(String name) {
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;
        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
        // Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
        for (BluetoothDevice deivce : mDevices) {
            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
            if (name.equals(deivce.getName())) {
                selectedDevice = deivce;
                break;
            }
        }
        return selectedDevice;
    }

    void beginListenForData() {
        final Handler handler = new Handler();

        Log.e("Test", "begin");

        readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
        readBuffer = new byte[1024];            // 수신 버퍼.

        // 문자열 수신 쓰레드.
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                while (!Thread.currentThread().isInterrupted()) {
                    try {
//                        Log.e("Test", "test");
                        // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
                        int byteAvailable = mInputStream.available();   // 수신 데이터 확인
                        if (byteAvailable > 0) {                        // 데이터가 수신된 경우.
                            Log.e("Test", byteAvailable + ".");
                            byte[] packetBytes = new byte[byteAvailable];
                            // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                            mInputStream.read(packetBytes);
                            for (int i = 0; i < byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                    //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        // 수신된 문자열 데이터에 대한 처리.
                                        @Override
                                        public void run() {
                                            // mStrDelimiter = '\n';

                                            Bundle bundle = new Bundle();
                                            bundle.putString("data",data);
                                            Log.e("Test", data);
                                            pagerAdapter.getItem(0).setArguments(bundle);

                                        }

                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                    Log.e("Test", "fail");
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                        Toast.makeText(getApplicationContext(), "데이터 수신 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
                        finish();            // App 종료.
                    }
                }
            }

        });

        mWorkerThread.start();
    }

    @Override
    protected void onDestroy() {
        try {
            mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mSocket.close();
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // startActivityForResult 를 여러번 사용할 땐 이런 식으로
        // switch 문을 사용하여 어떤 요청인지 구분하여 사용함.
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) { // 블루투스 활성화 상태
                    selectDevice();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 비활성화 상태 (종료)
                    Toast.makeText(getApplicationContext(), "블루투스를 사용할 수 없어 프로그램을 종료합니다",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
