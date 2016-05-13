import {selectorRegExpFactory, Bootloader, BootloaderConfig} from 'angular2-universal';

declare function registerJavaEngine(api:JavaEngine);

export interface IRenderCallback {
    (error:Error, result:string, requestId:number):void;
}

export interface IBootloaderConfigProvider {
    (requestInfo:any):BootloaderConfig;
}

export class JavaEngine {
    private _clientHtml:string;
    private _loadedBootloader:Bootloader;

    constructor(private _bootloaderConfigProvider:IBootloaderConfigProvider) {
    }

    public static connect(_bootloaderConfigProvider:IBootloaderConfigProvider) {
        registerJavaEngine(new JavaEngine(_bootloaderConfigProvider));
    }

    public setClientHtml(clientHtml:string) {
        this._clientHtml = clientHtml;
    }

    public render(requestInfo:any, renderId:number, callback:IRenderCallback) {
        let options = this._bootloaderConfigProvider(requestInfo) || <BootloaderConfig>{};
        options.providers = options.providers || undefined;
        options.preboot = options.preboot || undefined;

        if (!('directives' in options)) {
            throw new Error('Please provide an `directives` property with your Angular 2 application');
        }

        try {
            // bootstrap and render component to string
            const _options = options;
            if (!this._loadedBootloader) {
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
                this._loadedBootloader = bootloader;
            }

            this._loadedBootloader.serializeApplication(null, _options.providers)
                .then(html => callback(null, html, renderId))
                .catch(e => {
                    console.error(e.stack);
                    // if server fail then return client html
                    callback(null, this._clientHtml, renderId);
                });
        } catch (e) {
            callback(e, null, renderId);
        }
    }
}
