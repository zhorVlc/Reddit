import { Component } from '@angular/core';
import { NavController,NavParams } from 'ionic-angular';



@Component({
  selector: 'show',
  templateUrl: 'show.html'
})



export class ShowPage  {

    public title:any;
    public picture:any;
    public author:string;


  constructor(public navCtrl: NavController,public navParams:NavParams) {}



  ngOnInit(){

    try {
      this.title = this.navParams.data.item.data.title;
      this.author =  this.navParams.data.item.data.author;
      this.picture = this.navParams.data.item.data.preview.images[0].source.url;
    }
    catch(err) {
        this.picture = null;
    }
  }
}
