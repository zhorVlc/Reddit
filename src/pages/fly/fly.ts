import { Component , ViewChild } from '@angular/core';
import { NavController } from 'ionic-angular';
import { FormControl } from '@angular/forms';
import { PlaneService } from '../../providers/plane-service';
import { MapsService } from '../../providers/maps-service';
import { Map } from '../../../node_modules/mapbox-gl/dist/mapbox-gl.js';



@Component({
  selector: 'page-fly',
  templateUrl: 'fly.html',
  providers:[PlaneService,MapsService],
})
export class FlyPage  {
  @ViewChild('input') myInput ;

  result:any;              // Almaceno los aeropuertos recibidos por http



  searchingSalida:any = ' ';   // ngModel de la busqueda de aeropuerto Salida
  searchingDestino:any = ' ';   // ngModel de la busqueda de aeropuerto Destino
  searchControlSalida: FormControl;
  searchControlDestino: FormControl;
  resultSalida:any = false;    // controlo el spiner de busqueda de aeropuerto salida
  resultDestino:any = false;   // controlo el spiner de busqueda de aeropuerto destino

  constructor(public navCtrl: NavController,private _planeService:PlaneService,private mapService: MapsService) {
        this.searchControlSalida = new FormControl();
        this.searchControlDestino = new FormControl();
  }

    clickOrigen(item){
        this.resultSalida = false;
        this.searchingSalida = item.label;
    }
    clickDestino(item){
        this.resultDestino = false;
        this.searchingDestino = item.label;
    }

    CancelSearch(){
      this.resultSalida = false;
      this.resultDestino = false;
      this.result = null;
    }

  onSearchInputSalida(){
       this._planeService.getAirport(this.searchingSalida.trim()).subscribe((data)=>{
              this.resultSalida = true;
              this.result = data.items[0].entries;
      });

    }
    onSearchInputDestino(){
       this._planeService.getAirport(this.searchingDestino.trim()).subscribe((data)=>{
              this.resultDestino = true;
              this.result = data.items[0].entries;
      });

    }

    ngOnInit() {
    let map = new Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v10',
      zoom: 18,
      center: [-78.880453, 42.897852],
      logoPosition:'top-left',
      markers: [
      {
        lat: -78.880453,
        lng: 42.897852,
        title: 'Nice location',
        subtitle: 'Really really nice location'
      }
    ]
  });


    this.mapService.map = map;

  }




}
