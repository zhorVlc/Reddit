import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';
import { MyApp } from './app.component';
import { AboutPage } from '../pages/about/about';
import { RedditPage } from '../pages/reddit/reddit';
import { ShowPage } from '../pages/reddit/show/show';
import { SettingsPage } from '../pages/settings/settings';
import { TabsPage } from '../pages/tabs/tabs';

import {RedditService} from '../providers/reddit-service';


@NgModule({
  declarations: [
    MyApp,
    AboutPage,
    TabsPage,
    RedditPage,
    SettingsPage,
    ShowPage
  ],
  imports: [
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    AboutPage,
    TabsPage,
    RedditPage,
    SettingsPage,
    ShowPage
  ],
  providers: [{provide: ErrorHandler, useClass: IonicErrorHandler},RedditService]
})
export class AppModule {}
