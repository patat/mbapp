<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section class="container" data-js="app-root">
        <div class="manual-container">
            <div class="manual">
                <ol class="manual-list">
                    <li class="manual-list__item">Let the battle begin!</li>
                    <li class="manual-list__item">Choose the winner of each battle.</li>
                    <li class="manual-list__item">Behold the champion and give it your time.</li>
                </ol>
                <button class="main-btn" data-js="new-battle-btn">Let the battle begin</button>
            </div>
        </div>
    </section>
</@layout.noauthentication>