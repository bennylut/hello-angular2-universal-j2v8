import 'angular2-universal/polyfills';
import {JavaEngine} from './engine/java_engine.ts'

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

enableProdMode();

JavaEngine.connect((requestUrl:string) => {
    let baseUrl = '/';
    let url = requestUrl || '/';

    let config:ExpressEngineConfig = {
        directives: [App],
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
});