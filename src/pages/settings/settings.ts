import { Component } from '@angular/core';

import { NavController } from 'ionic-angular';

@Component({
  selector: 'settings',
  templateUrl: 'settings.html'
})
export class SettingsPage {

  public gaming:any;
  public limit:boolean;

  constructor(public navCtrl: NavController) {

  }
  ionViewWillEnter(){
    if(localStorage.getItem("limite")){
        this.gaming = localStorage.getItem("limite");
    }else{
       this.gaming = 10;
    }
  }

  changeSettings(event){
    this.limit = true;
    localStorage.setItem("limite",event);
  }
}
