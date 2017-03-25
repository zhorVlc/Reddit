import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';


@Injectable()
export class RedditService {
   http:any;
   baseUrl:string;

  constructor(http: Http) {
    this.http = http;
    this.baseUrl = "https://www.reddit.com/r";

  }

  getPost(category,limit){
    return  this.http.get(this.baseUrl+'/'+category+'/top.json?limit='+limit)
         .map(res=> res.json());


  }

}
