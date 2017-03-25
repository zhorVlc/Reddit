import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';

import { BikeService} from '../../../providers/bike-service';


@Component({
  selector: 'page-city',
  templateUrl: 'city.html',
  providers:[BikeService],
})
export class CityPage {

  city:string;
  items:any;
  constructor(public navCtrl: NavController,
              private bikeService:BikeService,
              public navParams: NavParams) {

      this.city = navParams.data.city.city;
  }

  ionViewWillEnter(){
      this.bikeService.getCityDetails(this.city).subscribe((details)=>{
          console.log(details);
          this.items = details;
      });
  }



}
