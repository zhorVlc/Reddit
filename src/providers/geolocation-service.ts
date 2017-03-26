import { Injectable ,ViewChild,ElementRef} from '@angular/core';
import {Geolocation} from 'ionic-native';


declare var google;

@Injectable()
export class GeolocationService {
  @ViewChild('map') mapElement: ElementRef;

      map: any;
      mapInitialised: boolean = false;
      apiKey = 'AIzaSyDXYq-nn5UCDYwEEwtP0Pi_zRL31qn2lSE';


  loadGoogleMaps(){

       this.addConnectivityListeners();

         if(typeof google == "undefined" || typeof google.maps == "undefined"){

           console.log("Google maps JavaScript needs to be loaded.");
           this.disableMap();

             //Load the SDK
             window['mapInit'] = () => {
               this.initMap();
               this.enableMap();
             }

             let script = document.createElement("script");
             script.id = "googleMaps";

             if(this.apiKey){
               script.src = 'http://maps.google.com/maps/api/js?key=' + this.apiKey + '&callback=mapInit';
             } else {
               script.src = 'http://maps.google.com/maps/api/js?callback=mapInit';
             }

             document.body.appendChild(script);

         }else {
             this.initMap();
             this.enableMap();

         }


     }

     initMap(){

        this.mapInitialised = true;

        Geolocation.getCurrentPosition().then((position) => {

          let latLng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

          let mapOptions = {
            center: latLng,
            zoom: 17,
            mapTypeId: google.maps.MapTypeId.TERRAIN
          }

          this.map = new google.maps.Map(this.mapElement.nativeElement, mapOptions);
          let marker = new google.maps.Marker({
                map: this.map,
                icon: {
                  path: google.maps.SymbolPath.CIRCLE,
                  scale: 10
                },
                position:latLng
            });

        });

      }

     addConnectivityListeners(){

            let onOnline = () => {

              setTimeout(() => {
                if(typeof google == "undefined" || typeof google.maps == "undefined"){

                  this.loadGoogleMaps();

                } else {

                  if(!this.mapInitialised){
                    this.initMap();
                  }

                  this.enableMap();
                }
              }, 2000);

            };

            let onOffline = () => {
              this.disableMap();
            };

            document.addEventListener('online', onOnline, false);
            document.addEventListener('offline', onOffline, false);

      }

     disableMap(){
      console.log("disable map");
    }

    enableMap(){
      console.log("enable map");
    }

}
