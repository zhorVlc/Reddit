import { Component ,ViewChild,ElementRef } from '@angular/core';
import { NavController, NavParams ,Platform} from 'ionic-angular';
import {SebmGoogleMap,GoogleMapsAPIWrapper} from 'angular2-google-maps/core';
import { StationPage } from '../station/station';
import { BikeService} from '../../../providers/bike-service';
import { GeolocationService } from '../../../providers/geolocation-service';


import {
 GoogleMaps,
 GoogleMap,
 GoogleMapsEvent,
 LatLng,
 CameraPosition,
 MarkerOptions,
 Marker
} from '@ionic-native/google-maps';

import {Geolocation} from 'ionic-native';
import { Truncate } from '../../../pipes/truncate';


declare var google;


@Component({
  selector: 'page-city',
  templateUrl: 'city.html',

  providers:[BikeService,GeolocationService,SebmGoogleMap
            ,GoogleMapsAPIWrapper,GoogleMaps],
//  pipes:[Truncate]
})
export class CityPage {
  @ViewChild('map') mapElement: ElementRef;


   map: GoogleMap;
  city:string;
  items:Array<any>;
  lat: number = null;
  lng: number = null;




  constructor(public navCtrl: NavController,
              private bikeService:BikeService,
              public geolocation:GeolocationService,
              private googleMaps: GoogleMaps,
              public MapsAPIWrapper:GoogleMapsAPIWrapper,
              private  maps : SebmGoogleMap,
              public platform: Platform,
              public navParams: NavParams) {

      this.city = navParams.data.city.city;


  }

  ionViewWillEnter(){
    Geolocation.getCurrentPosition().then((position)=>{
       this.lat = position.coords.latitude;
       this.lng = position.coords.longitude;


    });
      this.bikeService.getCityDetails(this.city).subscribe((details)=>{
        console.log(details)
          this.items = details;
      });
  }

  pushMarket(p){
    this.navCtrl.push(StationPage,{station:p});
  }
}
