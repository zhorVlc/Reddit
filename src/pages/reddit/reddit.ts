import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import {ShowPage} from './show/show';
import { RedditService} from '../../providers/reddit-service';

@Component({
  selector: 'reddit',
  templateUrl: 'reddit.html'
})
export class RedditPage {

  public items:any;
  public category:string="sports";
  public limit:any;
  constructor(public navCtrl: NavController,private RedditService:RedditService) {

  }
  ionViewWillEnter(){

    this.limit = localStorage.getItem("limite");
      this.getPost(this.category);
  }

  getPost(category){

    this.RedditService.getPost(category,this.limit).subscribe(response =>{

      this.items = response.data.children;
    });
  }

  openVideo(item){
     this.navCtrl.push(ShowPage,{item:item});

  }

  changeCategory(category){
      this.RedditService.getPost(category,this.limit).subscribe(response=>{
          this.items = response.data.children;
      })
  }


}
