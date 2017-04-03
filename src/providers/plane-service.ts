import { Injectable } from '@angular/core';
import { Http,Headers } from '@angular/http';

@Injectable()
export class PlaneService {


  http:Http;
  items:any;
  constructor(http: Http) {
    this.http = http;
  }

  getAirport(ciudad){
    const headers = new Headers();
        headers.append('Access-Control-Allow-Headers', 'Content-Type');
        headers.append('Access-Control-Allow-Methods', 'GET');
        headers.append('Content-Type', 'application/json');
        headers.append('Access-Control-Allow-Origin *','*');

    this.items = this.http.get("http://advanced.local/index.php?r=site/json&key="+ciudad)
        .map(res=>res.json());
        return this.items;


/*return this.http.get("http://lastminute.com/suggester/autocompleter?modules=airports&lang=es&key="+ciudad,{headers:headers})
      .map(res=>res.json());*/
 }


 filterItems(searchTerm){

        return this.items.filter((item) => {
            return item.title.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1;
        });

    }
}
