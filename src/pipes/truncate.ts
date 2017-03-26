import { Injectable, Pipe } from '@angular/core';


@Pipe({
  name: 'truncate'
})
@Injectable()
export class Truncate {

  transform(value, args) {
  return value.split('-');
  }
}
