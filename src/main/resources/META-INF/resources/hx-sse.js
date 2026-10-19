(() => {
    let api;

    function swapResponse(element, html) {
        const template = document.createElement('template');
        template.innerHTML = html;

        for (const outOfBand of template.content.querySelectorAll('[hx-swap-oob]')) {
            const target = document.querySelector(`#${CSS.escape(outOfBand.id)}`);
            if (target && outOfBand.getAttribute('hx-swap-oob') === 'outerHTML') {
                target.replaceWith(outOfBand);
            }
        }

        const targetSelector = api.attributeValue(element, 'hx-target') || 'this';
        const target = targetSelector === 'this' ? element : document.querySelector(targetSelector);
        const replacement = [...template.content.children]
            .find(candidate => !candidate.hasAttribute('hx-swap-oob'));
        if (target && replacement && api.attributeValue(element, 'hx-swap') === 'outerHTML') {
            target.replaceWith(replacement);
        }

        htmx.process(document.body);
    }

    function connect(element) {
        if (api.htmxProp(element).sse) return;

        const url = api.attributeValue(element, 'hx-sse:connect');
        if (!url) return;

        const eventSource = new EventSource(url);
        api.htmxProp(element).sse = eventSource;
        eventSource.onmessage = event => swapResponse(element, event.data);
        eventSource.onerror = () => {
            if (!element.isConnected) eventSource.close();
        };
    }

    function disconnect(element) {
        const eventSource = api.htmxProp(element).sse;
        if (!eventSource) return;

        eventSource.close();
        delete api.htmxProp(element).sse;
    }

    htmx.registerExtension('sse', {
        init: internalApi => {
            api = internalApi;
        },

        htmx_after_process: element => {
            if (api.attributeValue(element, 'hx-sse:connect')) connect(element);
            element.querySelectorAll?.('[hx-sse\\:connect]').forEach(connect);
        },

        htmx_before_cleanup: element => disconnect(element)
    });
})();
