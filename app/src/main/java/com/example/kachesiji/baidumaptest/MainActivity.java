package com.example.kachesiji.baidumaptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        findView();
        initBaiduMap();
    }

    private void findView() {
        mMapView = (MapView)findViewById(R.id.bdmapview);
        findViewById(R.id.setLoc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoc();
            }
        });
        findViewById(R.id.clearLoc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.remove();
            }
        });
        findViewById(R.id.searchTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initPOI();
                searchRoutePlan();
            }
        });
        findViewById(R.id.clearSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
            }
        });
    }

    private void searchRoutePlan() {
        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    transitRouteResult.getSuggestAddrInfo();
                    return;
                }
                if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    mBaiduMap.clear();
                    TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(transitRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }
        };
        routePlanSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        PlanNode node_1 = PlanNode.withCityNameAndPlaceName("北京", "龙泽");
        PlanNode node_2 = PlanNode.withCityNameAndPlaceName("北京", "西单");
        routePlanSearch.transitSearch(new TransitRoutePlanOption().from(node_1).city("北京").to(node_2));
        return;
    }

    private void setLoc() {
        LatLng point = new LatLng(39.963175, 116.400244);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location);
        MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).zIndex(9).draggable(true);
        marker = (Marker)mBaiduMap.addOverlay(option);
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });
    }

    private void initBaiduMap() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setTrafficEnabled(true);
    }

    private void initPOI() {
        PoiSearch poiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener onGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if(poiResult==null|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
                    return;
                }
                if(poiResult.error == SearchResult.ERRORNO.NO_ERROR){
                    mBaiduMap.clear();
                    PoiOverlay poiOverlay = new PoiOverlay(mBaiduMap);
                    poiOverlay.setData(poiResult);
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                    return;
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };
        poiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener);
        poiSearch.searchInCity(new PoiCitySearchOption().city("北京").keyword("美食").pageNum(10));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
