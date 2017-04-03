import { Component  } from '@angular/core';
import { NavController ,NavParams,ToastController} from 'ionic-angular';
import { StationPage } from '../station/station';
import { BikeService} from '../../../providers/bike-service';import {Geolocation} from 'ionic-native';


import { MapsService } from '../../../providers/maps-service';
import { Map } from '../../../../node_modules/mapbox-gl/dist/mapbox-gl.js';



@Component({
  selector: 'page-city',
  templateUrl: 'city.html',

  providers:[BikeService,MapsService],
//  pipes:[Truncate]
})

export class CityPage {


  city:string;
  public items:any;
  coords: {lat: number, lng: number};
  map:Map<any,any>;
  geoJSON = {
            "id": 'i.address',
            "type": "symbol",
            "source": {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                }
            },
            "layout": {
                "icon-image": "{icon}-15",
                "text-field": "{title}",
                "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
                "text-offset": [0, 0.6],
                "text-anchor": "top",
                 "icon-size": 2
            }


  };

  constructor(public navCtrl: NavController,
              private bikeService:BikeService,
              private navParams:NavParams,
              private toast:ToastController,
              private mapService: MapsService)  {}




 ionViewDidLoad() {
      this.city = this.navParams.data.city.city;
      Geolocation.getCurrentPosition().then((position)=>{
          this.coords = {
                         lat:position.coords.latitude,
                         lng:position.coords.longitude
                       }
          return position;
      }).then((position)=>{
        this.bikeService.getCityDetails(this.city).subscribe((details)=>{
               this.items = details;
             });

                var map = new Map({
                container: 'map',
                style: 'mapbox://styles/mapbox/streets-v9',
                zoom: 15,
                minZoom: 7,
                center: [this.coords.lng,this.coords.lat],
                logoPosition:'top-left',
                interactive: true
            });
           this.mapService.map = map;
            return map;

      }).then((map)=>{

        map.on("load",()=>{
         var features:any=[];
             this.items.forEach((v,i)=>{
                 features.push(
                   {
                   "type":"Feature",
                    "geometry":{
                         "type":"Point",
                         "coordinates":[v.position.lng,v.position.lat]
                    },
                    "properties":
                    {
                        "title": v.number,
                        "icon": "bicycle",
                        "description": v.address,
                    }
                 }
               );
             });

                 map.addLayer({
                   "id": 'point',
                   "type": "symbol",
                   "source": {
                       "type": "geojson",
                       "data": {
                           "type": "FeatureCollection",
                           "features":features,
                         },
                   },
                   "layout": {
                       "icon-image": "{icon}-15",
                       "text-field": "{title}",
                       "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
                       "text-offset": [0, 0.6],
                       "text-anchor": "top",
                        "icon-size": 2
                   }
                 });

                 map.on('click', ((e)=> {
                      let features  = map.queryRenderedFeatures(e.point,{layer:['point']});
                      if (features.length) {
                          this.items.filter((e)=>{
                            if(e.number == features[0].properties.title){
                               var toast = this.toast.create({
                                 message: e.address,
                                 duration: 3000,
                                 position: 'center'

                               });
                               toast.present();

                              this.pushMarket(e);
                             }
                        });
                      }
                 }));
            });
        });
  }

  Pickme(event){


  }


  pushMarket(p){
     let items:any = p;

    this.navCtrl.push(StationPage,{station:items});
  }
}
