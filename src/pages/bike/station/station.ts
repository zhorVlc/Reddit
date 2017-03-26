import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { Health } from '@ionic-native/health';


@Component({
  selector: 'page-station',
  templateUrl: 'station.html',
  providers:[Health]
})
export class StationPage {

  title:string;
  constructor(public navCtrl: NavController,
              public navParams: NavParams,
             private health: Health) {}

  ngOnInit(){
    this.health.isAvailable()
      .then(res => console.log(res))
      .catch(e => console.log(e));
      this.title =  this.navParams.data.station.address;
  }

}
