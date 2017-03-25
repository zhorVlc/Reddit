import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { CityPage } from '../bike/city/city';


@Component({
  selector: 'page-bike',
  templateUrl: 'bike.html'
})
export class BikePage  {

  items:Array<{abbr:string,city:string,icon:string,land:string}>;
  constructor(public navCtrl: NavController, public navParams: NavParams) {}


  ionViewWillEnter(){

    this.items = [
          {abbr:'AU',city:'Brisbane',icon:'AU',land:'Australia'},
          {abbr:'BE',city:'Bruxelles-Capitale',icon:'BE',land:'Belgica'},
          {abbr:'BE',city:'Namur',icon:'BE',land:'Belgica'},
          {abbr:'ES',city:'Santander',icon:'ES',land:'España'},
          {abbr:'ES',city:'Seville',icon:'ES',land:'España'},
          {abbr:'ES',city:'Valence',icon:'ES',land:'España'},
          {abbr:'FR',city:'Amiens',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Besancon',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Cergy-Pontoise',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Creteil',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Lyon',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Marseille',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Nancy',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Nantes',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Paris',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Rouen',icon:'FR',land:'Francia'},
          {abbr:'FR',city:'Toulouse',icon:'FR',land:'Francia'},
          {abbr:'IE',city:'Dublin',icon:'IE',land:'Irlanda'},
          {abbr:'JP',city:'Toyama',icon:'JP',land:'Japon'},
          {abbr:'LT',city:'Vilnius',icon:'LT',land:'Lituania'},
          {abbr:'LU',city:'Luxembourg',icon:'LU',land:'Luxemburgo'},
          {abbr:'NO',city:'Lillestrom',icon:'NO',land:'Noruega'},
          {abbr:'RU',city:'Kazan',icon:'RU',land:'Rusia'},

    ]
  }

  openCity(p){
       this.navCtrl.push(CityPage,{city:city});
  }
}
