import 'angular2-universal/polyfills';

import * as path from 'path';

// Angular 2 Universal
import {
  provide,
  enableProdMode,
  expressEngine,
  REQUEST_URL,
  ORIGIN_URL,
  BASE_URL,
  NODE_ROUTER_PROVIDERS,
  NODE_HTTP_PROVIDERS,
  ExpressEngineConfig
} from 'angular2-universal';

// Application
import {App} from './app/app.component';

interface IRenderCallback {
    (result:string, requestId:number):void;
}

enableProdMode();

class JavaNativeAPI {

  public indexPath = "src/main/angular2/index.html";

  public createRendringConfiguration(requestUrl:string) {
	  let baseUrl = '/';
	  let url = requestUrl || '/';

	  let config: ExpressEngineConfig = {
		directives: [ App ],
		platformProviders: [
		  provide(ORIGIN_URL, {useValue: 'http://localhost:3000'}),
		  provide(BASE_URL, {useValue: baseUrl}),
		],
		providers: [
		  provide(REQUEST_URL, {useValue: url}),
		  NODE_ROUTER_PROVIDERS,
		  NODE_HTTP_PROVIDERS,
		],
		async: true,
		preboot: false // { appRoot: 'app' } // your top level app component selector
	  };

	return config;
  }

  public render(url: string, callback:IRenderCallback, requestId: number) {
	expressEngine(this.indexPath, 
                      this.createRendringConfiguration(url),
                      (_, result) => callback(result, requestId));
  }

}

declare function registerJavaAPI(api: JavaNativeAPI);
registerJavaAPI(new JavaNativeAPI());


