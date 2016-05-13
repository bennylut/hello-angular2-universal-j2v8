import {selectorRegExpFactory, Bootloader, BootloaderConfig} from 'angular2-universal';

var BOOTLOADER; //no idea what it does yet...

declare function registerJavaEngine(api:JavaEngine);

export interface IRenderCallback {
    (error:Error, result:string, requestId:number):void;
}

export interface IBootloaderConfigProvider {
    (url:string):BootloaderConfig;
}

export class JavaEngine {
    private _clientHtml:string;

    constructor(private _bootloaderConfigProvider: IBootloaderConfigProvider){}

    public static connect(_bootloaderConfigProvider: IBootloaderConfigProvider) {
        registerJavaEngine(new JavaEngine(_bootloaderConfigProvider));
    }

    public setClientHtml(clientHtml: string) {
        this._clientHtml = clientHtml;
    }

    public render(url: string, renderId:number, callback:IRenderCallback) {
        let options = this._bootloaderConfigProvider(url) || <BootloaderConfig>{};
        options.providers = options.providers || undefined;
        options.preboot = options.preboot || undefined;

        if (!('directives' in options)) {
            throw new Error('Please provide an `directives` property with your Angular 2 application');
        }

        try {
            // bootstrap and render component to string
            const _options = options;
            if (!BOOTLOADER) {
                const _template = this._clientHtml;
                const _Bootloader = Bootloader;
                let bootloader = _options.bootloader;
                if (_options.bootloader) {
                    bootloader = _Bootloader.create(_options.bootloader);
                } else {
                    let doc = _Bootloader.parseDocument(_template);
                    _options.document = <string>doc;
                    _options.template = _options.template || _template;
                    bootloader = _Bootloader.create(_options);
                }
                BOOTLOADER = bootloader;
            }

            BOOTLOADER.serializeApplication(null, _options.providers)
                .then(html => {
                    callback(null, buildClientScripts(html, options), renderId);
                })
                .catch(e => {
                    console.error(e.stack);
                    // if server fail then return client html
                    callback(null, buildClientScripts(this._clientHtml, options), renderId);
                });
        } catch (e) {
            callback(e, null, renderId);
        }
    }

    // TODO: find better ways to configure the App initial state
    // to pay off this technical debt
    // currently checking for explicit values
    private _buildClientScripts(html:string, options:any):string {
        if (!options || !options.buildClientScripts) {
            return html;
        }
        return html
            .replace(
                selectorRegExpFactory('preboot'),
                ((options.preboot === false) ? '' : prebootScript(options))
            )
            .replace(
                selectorRegExpFactory('angular'),
                ((options.angular === false) ? '' : angularScript(options))
            )
            .replace(
                selectorRegExpFactory('bootstrap'),
                ((options.bootstrap === false) ? (
                    bootstrapButton +
                    bootstrapFunction(options)
                ) : (
                    (
                        (options.client === undefined || options.server === undefined) ?
                            '' : (options.client === false) ? '' : bootstrapButton
                    ) +
                    bootstrapFunction(options) +
                    ((options.client === false) ? '' : bootstrapApp)
                ))
            );
    }
}

// THE CODE BELOW COPIED DIRECTLY FROM angular-express-engine


function prebootScript(config):string {
    let baseUrl = (config && config.preboot && config.preboot.baseUrl) || '/preboot';
    return `
  <preboot>
    <link rel="stylesheet" type="text/css" href="${baseUrl}/preboot.css">
    <script src="${baseUrl}/preboot.js"></script>
    <script>preboot.start()</script>
  </preboot>
  `;
}

function angularScript(config):string {
    let systemConfig = (config && config.systemjs) || {};
    let baseUrl = systemConfig.nodeModules || '/node_modules';
    let newConfig = (<any>Object).assign({}, {
        baseURL: '/',
        defaultJSExtensions: true
    }, systemConfig);
    return `
  <!-- Browser polyfills -->
  <script src="${baseUrl}/es6-shim/es6-shim.min.js"></script>
  <script src="${baseUrl}/systemjs/dist/system-polyfills.js"></script>
  <script src="${baseUrl}/angular2/bundles/angular2-polyfills.min.js"></script>
  <!-- SystemJS -->
  <script src="${baseUrl}/systemjs/dist/system.js"></script>
  <!-- Angular2: Bundle -->
  <script src="${baseUrl}/rxjs/bundles/Rx.js"></script>
  <script src="${baseUrl}/angular2/bundles/angular2.dev.js"></script>
  <script src="${baseUrl}/angular2/bundles/router.dev.js"></script>
  <script src="${baseUrl}/angular2/bundles/http.dev.js"></script>
  <script type="text/javascript">
  System.config(${ JSON.stringify(newConfig) });
  </script>
  `;
};

const bootstrapButton:string = `
  <div id="bootstrapButton">
    <style>
     #bootstrapButton {
      z-index:999999999;
      position: absolute;
      background-color: rgb(255, 255, 255);
      padding: 0.5em;
      border-radius: 3px;
      border: 1px solid rgb(207, 206, 206);
     }
    </style>
    <button onclick="bootstrap()">
      Bootstrap Angular2 Client
    </button>
  </div>
`;

var bootstrapApp = `
  <script>
    setTimeout(function() {
      bootstrap();
    });
  </script>
`;

function bootstrapFunction(config:any):string {
    let systemConfig = (config && config.systemjs) || {};
    let url = systemConfig.componentUrl;
    return `
  <script>
    function bootstrap() {
      if (this.bootstraped) return;
      this.bootstraped = true;
      System.import("${ url }")
        .then(function(module) {
          return module.main();
        })
        .then(function() {
          if ('preboot' in window) { preboot.complete(); }
          var $bootstrapButton = document.getElementById("bootstrapButton");
          if ($bootstrapButton) { $bootstrapButton.remove(); }
        });
    }
  </script>
`;
};

// TODO: find better ways to configure the App initial state
// to pay off this technical debt
// currently checking for explicit values
function buildClientScripts(html:string, options:any):string {
    if (!options || !options.buildClientScripts) {
        return html;
    }
    return html
        .replace(
            selectorRegExpFactory('preboot'),
            ((options.preboot === false) ? '' : prebootScript(options))
        )
        .replace(
            selectorRegExpFactory('angular'),
            ((options.angular === false) ? '' : angularScript(options))
        )
        .replace(
            selectorRegExpFactory('bootstrap'),
            ((options.bootstrap === false) ? (
                bootstrapButton +
                bootstrapFunction(options)
            ) : (
                (
                    (options.client === undefined || options.server === undefined) ?
                        '' : (options.client === false) ? '' : bootstrapButton
                ) +
                bootstrapFunction(options) +
                ((options.client === false) ? '' : bootstrapApp)
            ))
        );
}