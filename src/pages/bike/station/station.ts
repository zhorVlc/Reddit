import { Component ,OnInit} from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';


@Component({
  selector: 'page-station',
  templateUrl: 'station.html',
})
export class StationPage implements OnInit {



  public update:any;
  public name:any;
  nav:any;


  constructor(public navCtrl: NavController,
              public navParams: NavParams) {
                this.nav = navParams;
              }

  ngOnInit(){


    /* this.Data.title =  this.nav.data.station.address;
     console.log(this.Data.title)
      this.Data.total =  this.nav.data.station.bike_stands;
      this.Data.disponibles = this.nav.data.station.available_bikes;
      this.Data.uso = this.nav.data.station.available_bike_stands;
      this.Data.number =  this.nav.data.station.number;
      this.Data.status = this.nav.data.station.status;
      this.Data.update = this.nav.data.station.last_update;
      this.Data.ubicacion = this.nav.data.station.name;
      this.Data.lat = this.nav.data.station.position.lat;
      this.Data.lng = this.nav.data.station.position.lng;*/
      var date = new Date(this.nav.data.station.last_update);
      this.update = ('0' + date.getDate()).slice(-2) + '/' + ('0' + (date.getMonth() + 1)).slice(-2) + '/' + date.getFullYear() + ' ' + ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
     this.name = this.nav.data.station.name.replace(/[0-9]/g,"").replace(/[_]/g," ");;


  }

}
