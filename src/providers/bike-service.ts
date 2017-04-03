import { Injectable } from '@angular/core';
import { Http  } from '@angular/http';
import 'rxjs/add/operator/map';


@Injectable()
export class BikeService {

  api:string = '2514802787772a429641e1f30bf485fcc4db70cd';
  city:string = null;
  http:Http;
  constructor(http: Http) {
      this.http = http;
  }

  getCityDetails(city){

     this.city = city;
     return this.http.get('https://api.jcdecaux.com/vls/v1/stations?contract='+this.city+'&apiKey='+this.api+'')
              .map(res=>res.json());

  }

}
