import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';
import { MyApp } from './app.component';
import { AboutPage } from '../pages/about/about';
import { RedditPage } from '../pages/reddit/reddit';
import { ShowPage } from '../pages/reddit/show/show';
import { SettingsPage } from '../pages/settings/settings';
import { TabsPage } from '../pages/tabs/tabs';
import { BikePage } from '../pages/bike/bike';
import { CityPage } from '../pages/bike/city/city';
import { StationPage } from '../pages/bike/station/station';


import { AgmCoreModule } from 'angular2-google-maps/core';

import {RedditService} from '../providers/reddit-service';
import { BikeService } from '../providers/bike-service';
import { GeolocationService } from '../providers/geolocation-service';
import { Truncate } from '../pipes/truncate';


@NgModule({
  declarations: [
    MyApp,
    AboutPage,
    TabsPage,
    RedditPage,
    SettingsPage,
    ShowPage,
    BikePage,
    CityPage,
    Truncate,
    StationPage,

  ],
  imports: [
    IonicModule.forRoot(MyApp),
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBpgyicTGWpxTOfwyWS-8wW7E5zmnOd2do'
    })
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    AboutPage,
    TabsPage,
    RedditPage,
    SettingsPage,
    ShowPage,
    BikePage,
    CityPage,
    StationPage,
  ],
  providers: [
   {provide: ErrorHandler, useClass: IonicErrorHandler},RedditService,BikeService

  ]
})
export class AppModule {}
