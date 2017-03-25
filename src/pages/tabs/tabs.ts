import { Component } from '@angular/core';

import { AboutPage } from '../about/about';
import { RedditPage } from '../reddit/reddit';
import { SettingsPage } from '../settings/settings';
import { BikePage } from '../bike/bike';


@Component({
  templateUrl: 'tabs.html'
})
export class TabsPage {
  // this tells the tabs component which Pages
  // should be each tab's root Page
  tab1Root: any = RedditPage;
  tab2Root: any = BikePage;
  tab3Root: any = SettingsPage;
  tab4Root: any = AboutPage;

  constructor() {

  }
}
