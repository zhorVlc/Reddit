import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import * as mapboxgl from '../../node_modules/mapbox-gl/dist/mapbox-gl.js';
import { Map } from '../../node_modules/mapbox-gl/dist/mapbox-gl.js';


@Injectable()
export class MapsService {
map: Map<any, any>;

  constructor() {
    (mapboxgl as any).accessToken ="pk.eyJ1IjoiamFubzEiLCJhIjoiY2owdTJ2ODN5MDU5azMzancwbzA1dXlhdyJ9.yRKq9F5vNpngmhZIikL_1Q";
  }


  
}
