/**
 * AWS Lambda - Playwright 뉴스레터 구독 자동 해지 엔진
 */

const chromium = require('@sparticuz/chromium');
const { chromium: playwright } = require('playwright-core');

const USER_AGENT =
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36';

const BLOCKED_RESOURCE_TYPES = ['image', 'font', 'media'];

const RENDER_WAIT_TIMEOUT_MS = 20000;
const UNSUBSCRIBE_TIMEOUT_MS = 20000;
const NAVIGATION_TIMEOUT_MS = 60000;
const POLLING_INTERVAL_MS = 500;

exports.handler = async (event) => {
    const startTime = Date.now();
    console.log('🚀 [START] Lambda 실행 시작');
    console.log('📦 [EVENT] 데이터:', JSON.stringify(event));

    const logMemory = (tag = 'Status') => {
        const used = process.memoryUsage();
        console.log(
            `📊 [MEMORY ${tag}]: rss=${Math.round(
                used.rss / 1024 / 1024,
            )}MB, heapUsed=${Math.round(used.heapUsed / 1024 / 1024)}MB`,
        );
    };

    logMemory('Initial');

    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event;
    let { url, patterns } = body;

    if (!url) {
        console.error('❌ [ERROR] 해지 URL이 누락되었습니다.');
        return {
            success: false,
            statusCode: 400,
            message: '해지 URL이 누락되었습니다.',
        };
    }

    if (!url.startsWith('http://') && !url.startsWith('https://')) {
        console.log(`🔧 [FIX] URL 프로토콜 누락 감지, https:// 추가: ${url}`);
        url = 'https://' + url;
    }

    const regex = {
        unsubscribe: new RegExp(
            patterns?.unsubscribe ||
            'unsubscribe|구독.?취소|수신.?거부|해지|cancel|confirm|yes',
            'i',
        ),
        success: new RegExp(
            patterns?.success ||
            'unsubscribed|canceled|cancelled|successfully|취소.?완료|처리.?완료|해지.?완료|거부.?완료|취소.?되었습니다|해지.?되었습니다|성공|완료',
            'i',
        ),
        alreadyUnsubscribed: new RegExp(
            patterns?.alreadyUnsubscribed ||
            'already.?unsubscribed|not.?subscribed|no.?longer|이미.?구독.?취소|이미.?취소|이미.?수신.?거부|구독.?취소.?되었습니다|해지.?되었습니다|메일러가.?없어요',
            'i',
        ),
        error: new RegExp(patterns?.error || 'error|failed|실패|오류', 'i'),
    };

    const adDomains = patterns?.adDomains || [
        'doubleclick.net',
        'google-analytics.com',
        'googletagmanager.com',
    ];

    const resubscribeRegex =
        /resubscribe|re-subscribe|subscribe again|다시.?구독|재구독/i;

    console.log(
        '🧩 [PATTERN] 적용 패턴:',
        JSON.stringify({
            unsubscribe: patterns?.unsubscribe,
            success: patterns?.success,
            alreadyUnsubscribed: patterns?.alreadyUnsubscribed,
            error: patterns?.error,
            adDomains,
        }),
    );

    let browser = null;
    let isProcessed = false;
    let hasError = false;
    let hasClicked = false;

    try {
        console.log('🌐 [STEP 1] 브라우저 실행 준비 중...');

        browser = await playwright.launch({
            args: [...chromium.args, '--disable-gpu', '--disable-dev-shm-usage'],
            executablePath: await chromium.executablePath(),
            headless: true,
        });

        console.log('✅ [STEP 1] 브라우저 실행 완료');
        logMemory('BrowserUp');

        const context = await browser.newContext({
            userAgent: USER_AGENT,
            viewport: { width: 1280, height: 720 },
            extraHTTPHeaders: {
                'Accept-Language': 'ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7',
                Referer: 'https://www.google.com/',
            },
            locale: 'ko-KR',
            timezoneId: 'Asia/Seoul',
        });

        await context.route('**/*', async (route) => {
            try {
                const type = route.request().resourceType();
                if (BLOCKED_RESOURCE_TYPES.includes(type)) {
                    await route.abort();
                } else {
                    await route.continue();
                }
            } catch {
                try {
                    await route.continue();
                } catch {}
            }
        });

        const page = await context.newPage();

        const cleanForLog = (text, maxLength = 120) => {
            return String(text || '')
                .replace(/\s+/g, ' ')
                .trim()
                .substring(0, maxLength);
        };

        const getElementText = async (locator) => {
            const [innerText, textContent, value, ariaLabel, title] =
                await Promise.all([
                    locator.innerText().catch(() => ''),
                    locator.textContent().catch(() => ''),
                    locator.getAttribute('value').catch(() => ''),
                    locator.getAttribute('aria-label').catch(() => ''),
                    locator.getAttribute('title').catch(() => ''),
                ]);

            return [innerText, textContent, value, ariaLabel, title]
                .filter(Boolean)
                .join(' ')
                .replace(/\s+/g, ' ')
                .trim();
        };

        const getDetectionText = async (frame) => {
            return await frame
                .locator('body')
                .evaluate((body) => {
                    const clone = body.cloneNode(true);

                    clone
                        .querySelectorAll(
                            [
                                'script',
                                'style',
                                'noscript',
                                'button',
                                'a',
                                'input',
                                'select',
                                'textarea',
                                '[role="button"]',
                            ].join(','),
                        )
                        .forEach((el) => el.remove());

                    return (clone.innerText || clone.textContent || '')
                        .replace(/\s+/g, ' ')
                        .trim();
                })
                .catch(() => '');
        };

        const checkSuccessText = async (contextName = 'auto', options = {}) => {
            const includeSuccess = options.includeSuccess ?? true;

            try {
                const frames = page.frames();
                console.log(
                    `🧾 [TEXT 검사 시작] context=${contextName}, includeSuccess=${includeSuccess}, frameCount=${frames.length}`,
                );

                for (const frame of frames) {
                    const detectionText = await getDetectionText(frame);
                    const frameUrl = frame.url();

                    if (!detectionText) {
                        if (contextName !== 'polling') {
                            console.log(
                                `🧾 [TEXT 검사] context=${contextName}, frameUrl=${frameUrl}, length=0`,
                            );
                        }
                        continue;
                    }

                    if (contextName !== 'polling') {
                        console.log(
                            `🧾 [TEXT 검사] context=${contextName}, frameUrl=${frameUrl}, length=${detectionText.length}, text="${cleanForLog(
                                detectionText,
                            )}..."`,
                        );
                    }

                    const matchedBySuccess =
                        includeSuccess && regex.success.test(detectionText);
                    const matchedByAlready =
                        regex.alreadyUnsubscribed.test(detectionText);

                    if (matchedBySuccess || matchedByAlready) {
                        const reason = matchedByAlready
                            ? 'already-unsubscribed'
                            : 'success';
                        console.log(
                            `✨ [TEXT 성공 감지] context=${contextName}, reason=${reason}, frameUrl=${frameUrl}, text="${cleanForLog(
                                detectionText,
                            )}..."`,
                        );
                        return true;
                    }
                }
            } catch (e) {
                console.log(
                    `⚠️ [TEXT 확인 중 오류] context=${contextName}, error=${e.message}`,
                );
            }

            return false;
        };

        const getButtonState = async () => {
            const frames = page.frames();

            for (const frame of frames) {
                const frameUrl = frame.url();

                const state = await frame
                    .evaluate(
                        ({ unsubscribePattern, alreadyPattern, errorPattern }) => {
                            const unsubscribeRegex = new RegExp(unsubscribePattern, 'i');
                            const alreadyRegex = new RegExp(alreadyPattern, 'i');
                            const errorRegex = new RegExp(errorPattern, 'i');

                            const bodyText = (document.body?.innerText || '')
                                .replace(/\s+/g, ' ')
                                .trim();

                            const candidates = [
                                ...document.querySelectorAll(
                                    'button, a, [role="button"], input[type="button"], input[type="submit"]',
                                ),
                            ].map((el) => {
                                const text = [
                                    el.innerText,
                                    el.textContent,
                                    el.value,
                                    el.getAttribute('aria-label'),
                                    el.getAttribute('title'),
                                ]
                                    .filter(Boolean)
                                    .join(' ')
                                    .replace(/\s+/g, ' ')
                                    .trim();

                                const visible = !!(
                                    el.offsetWidth ||
                                    el.offsetHeight ||
                                    el.getClientRects().length
                                );

                                return {
                                    text,
                                    visible,
                                    tag: el.tagName,
                                    html: el.outerHTML.slice(0, 240),
                                };
                            });

                            return {
                                bodyText,
                                hasUnsubscribeButton: candidates.some(
                                    (candidate) =>
                                        candidate.visible && unsubscribeRegex.test(candidate.text),
                                ),
                                hasAnyUnsubscribeCandidate: candidates.some((candidate) =>
                                    unsubscribeRegex.test(candidate.text),
                                ),
                                hasAlreadyText: alreadyRegex.test(bodyText),
                                hasExplicitErrorText:
                                    errorRegex.test(bodyText) ||
                                    /404|not found|페이지를 찾을 수|존재하지 않는|만료된|invalid|expired/i.test(
                                        bodyText,
                                    ),
                                candidates,
                            };
                        },
                        {
                            unsubscribePattern: regex.unsubscribe.source,
                            alreadyPattern: regex.alreadyUnsubscribed.source,
                            errorPattern: regex.error.source,
                        },
                    )
                    .catch((error) => ({
                        bodyText: '',
                        hasUnsubscribeButton: false,
                        hasAnyUnsubscribeCandidate: false,
                        hasAlreadyText: false,
                        hasExplicitErrorText: false,
                        candidates: [],
                        evaluateError: error.message,
                    }));

                if (
                    state.hasUnsubscribeButton ||
                    state.hasAnyUnsubscribeCandidate ||
                    state.hasAlreadyText ||
                    state.hasExplicitErrorText
                ) {
                    return {
                        frameUrl,
                        ...state,
                    };
                }
            }

            const mainFrame = page.mainFrame();
            const bodyText = await mainFrame.locator('body').innerText().catch(() => '');

            return {
                frameUrl: mainFrame.url(),
                bodyText,
                hasUnsubscribeButton: false,
                hasAnyUnsubscribeCandidate: false,
                hasAlreadyText: false,
                hasExplicitErrorText: false,
                candidates: [],
            };
        };

        const waitForActionableUnsubscribeState = async () => {
            const deadline = Date.now() + RENDER_WAIT_TIMEOUT_MS;

            while (Date.now() < deadline) {
                const state = await getButtonState();

                console.log(
                    `⏳ [WAIT] frameUrl=${state.frameUrl}, hasButton=${state.hasUnsubscribeButton}, hasAnyCandidate=${state.hasAnyUnsubscribeCandidate}, hasAlready=${state.hasAlreadyText}, hasError=${state.hasExplicitErrorText}, candidateCount=${state.candidates.length}, body="${cleanForLog(
                        state.bodyText,
                        180,
                    )}"`,
                );

                if (state.hasUnsubscribeButton) {
                    return 'FOUND_BUTTON';
                }

                if (state.hasAlreadyText) {
                    return 'ALREADY_UNSUBSCRIBED';
                }

                if (state.hasExplicitErrorText) {
                    return 'ERROR_PAGE';
                }

                await page.waitForTimeout(POLLING_INTERVAL_MS);
            }

            console.log('⏱️ [WAIT] 해지 버튼 렌더링 대기 시간 초과');
            return 'TIMEOUT';
        };

        const logAllButtonCandidates = async () => {
            const selector =
                'button, a, [role="button"], input[type="button"], input[type="submit"]';

            for (const frame of page.frames()) {
                const frameUrl = frame.url();
                const bodyText = await frame.locator('body').innerText().catch(() => '');
                const candidates = frame.locator(selector);
                const count = await candidates.count();

                console.log(
                    `🧪 [DEBUG BUTTONS] frameUrl=${frameUrl}, hasUnsubscribeText=${bodyText.includes(
                        '구독 취소',
                    )}, allCandidateCount=${count}`,
                );

                const summary = [];

                for (let i = 0; i < count; i++) {
                    const candidate = candidates.nth(i);
                    const [text, visible, tagName, type, href, html] = await Promise.all([
                        getElementText(candidate),
                        candidate
                            .evaluate(
                                (el) =>
                                    !!(
                                        el.offsetWidth ||
                                        el.offsetHeight ||
                                        el.getClientRects().length
                                    ),
                            )
                            .catch(() => false),
                        candidate.evaluate((el) => el.tagName).catch(() => ''),
                        candidate.getAttribute('type').catch(() => ''),
                        candidate.getAttribute('href').catch(() => ''),
                        candidate
                            .evaluate((el) => el.outerHTML.slice(0, 300))
                            .catch(() => ''),
                    ]);

                    summary.push({
                        i,
                        tag: tagName,
                        type: type || '-',
                        href: href || '-',
                        visible,
                        text: cleanForLog(text, 160),
                        html: cleanForLog(html, 220),
                    });
                }

                console.log(
                    `🧪 [DEBUG BUTTONS DETAIL] frameUrl=${frameUrl}, candidates=${JSON.stringify(
                        summary,
                    )}`,
                );
            }
        };

        const findUnsubscribeButton = async () => {
            const visibleSelector =
                'button:visible, a:visible, [role="button"]:visible, input[type="button"]:visible, input[type="submit"]:visible';

            await logAllButtonCandidates();

            for (const frame of page.frames()) {
                const frameUrl = frame.url();
                const buttons = frame.locator(visibleSelector);
                const count = await buttons.count();

                console.log(`🔘 [BUTTON] frameUrl=${frameUrl}, 가시적 후보 수: ${count}`);

                for (let i = 0; i < count; i++) {
                    const btn = buttons.nth(i);

                    const [buttonText, tagName, type, href] = await Promise.all([
                        getElementText(btn),
                        btn.evaluate((el) => el.tagName).catch(() => ''),
                        btn.getAttribute('type').catch(() => ''),
                        btn.getAttribute('href').catch(() => ''),
                    ]);

                    console.log(
                        `🔎 [BUTTON 후보 ${i + 1}/${count}] frameUrl=${frameUrl}, tag=${tagName}, type=${
                            type || '-'
                        }, href=${href || '-'}, text="${cleanForLog(buttonText)}"`,
                    );

                    if (!buttonText) {
                        console.log(`⏭️ [BUTTON 제외 ${i + 1}] 텍스트 없음`);
                        continue;
                    }

                    if (resubscribeRegex.test(buttonText)) {
                        console.log(`⏭️ [BUTTON 제외 ${i + 1}] 재구독 버튼 감지`);
                        continue;
                    }

                    if (!regex.unsubscribe.test(buttonText)) {
                        console.log(`⏭️ [BUTTON 제외 ${i + 1}] 해지 패턴 불일치`);
                        continue;
                    }

                    console.log(`🎯 [BUTTON 선택 ${i + 1}] "${cleanForLog(buttonText, 160)}"`);
                    return btn;
                }
            }

            console.log('🔍 [BUTTON] 해지 버튼 후보를 찾지 못했습니다.');
            return null;
        };

        page.on('requestfailed', (request) => {
            console.log(
                `❌ [REQUEST FAILED] method=${request.method()}, url=${request.url()}, failure=${request.failure()?.errorText}`,
            );
        });

        page.on('dialog', async (dialog) => {
            try {
                const message = dialog.message();
                console.log(
                    `💬 [DIALOG 감지] hasClicked=${hasClicked}, message="${cleanForLog(
                        message,
                        200,
                    )}"`,
                );

                if (regex.error.test(message)) {
                    console.log('⚠️ [DIALOG 에러 감지]');
                    hasError = true;
                }

                if (
                    hasClicked &&
                    (regex.success.test(message) ||
                        regex.alreadyUnsubscribed.test(message))
                ) {
                    console.log('✨ [DIALOG 성공 감지]');
                    isProcessed = true;
                }

                if (!hasClicked && regex.alreadyUnsubscribed.test(message)) {
                    console.log('✨ [DIALOG 이미해지 감지]');
                    isProcessed = true;
                }

                await dialog.accept();
            } catch (e) {
                console.error('⚠️ [DIALOG 오류]:', e);
            }
        });

        page.on('response', async (response) => {
            try {
                const resUrl = response.url();
                const status = response.status();
                const lowerUrl = resUrl.toLowerCase();

                if (
                    adDomains.some((domain) =>
                        lowerUrl.includes(String(domain).toLowerCase()),
                    )
                ) {
                    return;
                }

                if (lowerUrl.includes('unsubscribe') || status >= 400) {
                    console.log(
                        `📡 [RESPONSE] clicked=${hasClicked}, status=${status}, url=${resUrl.substring(
                            0,
                            180,
                        )}`,
                    );
                }

                if (!hasClicked) return;

                if (status >= 200 && status < 400 && lowerUrl.includes('unsubscribe')) {
                    console.log(`📡 [XHR 성공 감지] status=${status}`);
                    isProcessed = true;
                }

                if (status >= 400 && status < 500 && lowerUrl.includes('unsubscribe')) {
                    const resBody = await response.text().catch(() => '');
                    console.log(
                        `📡 [XHR 4xx 본문] status=${status}, body="${cleanForLog(
                            resBody,
                            200,
                        )}"`,
                    );

                    if (regex.alreadyUnsubscribed.test(resBody)) {
                        console.log(`📡 [XHR 이미해지 감지] status=${status}`);
                        isProcessed = true;
                    } else {
                        console.log(`📡 [XHR 에러 감지] status=${status}`);
                        hasError = true;
                    }
                }
            } catch (e) {
                console.warn('⚠️ [RESPONSE 오류]:', e.message);
            }
        });

        console.log(`🔗 [STEP 2] 페이지 접속 시도: ${url}`);

        let navigationResponse = null;

        try {
            navigationResponse = await page.goto(url, {
                waitUntil: 'networkidle',
                timeout: NAVIGATION_TIMEOUT_MS,
            });
        } catch (gotoErr) {
            console.warn(
                `⚠️ [STEP 2] networkidle 실패, load로 재시도: ${gotoErr.message}`,
            );
            navigationResponse = await page.goto(url, {
                waitUntil: 'load',
                timeout: NAVIGATION_TIMEOUT_MS,
            });
        }

        const initialStatus = navigationResponse?.status();
        const pageTitle = await page.title().catch(() => 'No Title');

        console.log(
            `✅ [STEP 2] 페이지 로드 완료 (status=${initialStatus}, Title="${pageTitle}", URL=${page.url()})`,
        );

        if (initialStatus === 404 || initialStatus === 410) {
            return {
                success: false,
                statusCode: initialStatus,
                message: '해지 페이지가 존재하지 않거나 만료되었습니다.',
            };
        }

        console.log('⏳ 해지 화면 렌더링 대기 중...');
        const waitResult = await waitForActionableUnsubscribeState();

        if (waitResult === 'ERROR_PAGE') {
            return {
                success: false,
                statusCode: 404,
                message: '해지 페이지가 존재하지 않거나 오류 페이지로 렌더링되었습니다.',
            };
        }

        if (waitResult === 'ALREADY_UNSUBSCRIBED') {
            return {
                success: true,
                statusCode: 200,
                message: '이미 해지된 상태 문구가 발견되었습니다.',
                method: 'ALREADY_UNSUBSCRIBED',
            };
        }

        logMemory('PageLoaded');

        const beforeUrl = page.url();

        console.log('🔍 [STEP 3] 해지 버튼 우선 검색 중...');
        const targetButton = await findUnsubscribeButton();

        if (targetButton) {
            console.log(`🖱️ [STEP 4] 해지 버튼 클릭 시도. beforeUrl=${beforeUrl}`);
            hasClicked = true;

            await targetButton.click();
            console.log(`🖱️ [CLICK] 클릭 직후 URL: ${page.url()}`);

            await page.waitForTimeout(500);
            console.log(`🖱️ [CLICK] 500ms 후 URL: ${page.url()}`);

            const deadline = Date.now() + UNSUBSCRIBE_TIMEOUT_MS;

            while (Date.now() < deadline) {
                if (isProcessed) {
                    console.log('🎯 [RESULT] 네트워크 응답(XHR)으로 성공 확인');
                    return {
                        success: true,
                        statusCode: 200,
                        message: '백그라운드 네트워크 통신을 통해 해지 승인을 확인했습니다.',
                        method: 'NETWORK_CONFIRMATION',
                    };
                }

                if (hasError) {
                    console.log('🛑 [POLLING] hasError=true, 폴링 중단');
                    break;
                }

                if (page.url() !== beforeUrl) {
                    console.log(
                        `🔄 [REDIRECT] URL 변경 감지: before=${beforeUrl}, after=${page.url()}`,
                    );
                    await page.waitForTimeout(1000);

                    if (
                        await checkSuccessText('after_navigation', {
                            includeSuccess: true,
                        })
                    ) {
                        console.log('🎯 [RESULT] 페이지 이동 후 문구로 성공 확인');
                        return {
                            success: true,
                            statusCode: 200,
                            message: '페이지 이동 후 화면에서 성공 문구가 포착되었습니다.',
                            method: 'NAVIGATION_SUCCESS',
                        };
                    }

                    console.log('🎯 [RESULT] URL 변경으로 성공 간주');
                    return {
                        success: true,
                        statusCode: 200,
                        message:
                            '해지 프로세스 완료 후 페이지가 이동되어 성공한 것으로 간주합니다.',
                        method: 'NAVIGATION_SUCCESS',
                    };
                }

                if (await checkSuccessText('polling', { includeSuccess: true })) {
                    console.log('🎯 [RESULT] 화면 문구 폴링으로 성공 확인');
                    return {
                        success: true,
                        statusCode: 200,
                        message: '클릭 후 화면에 성공 문구가 나타난 것을 확인했습니다.',
                        method: 'SCREEN_TEXT_MATCH',
                    };
                }

                await page.waitForTimeout(POLLING_INTERVAL_MS);
            }
        }

        if (!targetButton) {
            console.log('🔍 [STEP 4] 해지 버튼 없음 - 이미 해지 상태 문구 확인 중...');

            if (await checkSuccessText('no_button_already', { includeSuccess: false })) {
                console.log('🎯 [RESULT] 해지 버튼 없음 + 이미 해지 상태 확인');
                return {
                    success: true,
                    statusCode: 200,
                    message: '해지 버튼은 없고 이미 해지된 상태 문구가 발견되었습니다.',
                    method: 'ALREADY_UNSUBSCRIBED',
                };
            }
        }

        if (isProcessed) {
            console.log('🎯 [RESULT] 최종 응답(XHR)으로 성공 확인');
            return {
                success: true,
                statusCode: 200,
                message: '최종 네트워크 응답 결과가 성공으로 판정되었습니다.',
                method: 'NETWORK_CONFIRMATION',
            };
        }

        if (await checkSuccessText('final', { includeSuccess: hasClicked })) {
            console.log('🎯 [RESULT] 최종 화면 문구로 성공 확인');
            return {
                success: true,
                statusCode: 200,
                message: '최종적으로 화면에서 성공 문구가 발견되었습니다.',
                method: 'SCREEN_TEXT_MATCH',
            };
        }

        console.warn('🛑 [RESULT] 성공 결과를 확인하지 못함');

        const finalUrl = page.url();
        const html = await page.content().catch(() => '');
        const finalBody = await page
            .locator('body')
            .evaluate((body) => {
                const clone = body.cloneNode(true);
                clone
                    .querySelectorAll('script, style, noscript')
                    .forEach((el) => el.remove());
                return clone.innerText || clone.textContent || '';
            })
            .catch(() => '');

        console.log(
            `🧭 [DEBUG] hasClicked=${hasClicked}, isProcessed=${isProcessed}, hasError=${hasError}, finalUrl=${finalUrl}`,
        );
        console.log(`📄 [DEBUG] HTML 길이: ${html.length}`);
        console.log(`📄 [DEBUG] HTML 앞부분: ${html.substring(0, 5000)}`);
        console.log(
            `🔍 [DEBUG] 최종 본문 텍스트: "${cleanForLog(finalBody, 300)}..."`,
        );

        return {
            success: false,
            statusCode: hasError ? 500 : 404,
            message: hasError
                ? '사이트에서 실패 관련 팝업 혹은 에러가 감지되었습니다.'
                : '정해진 시간 내에 성공 결과를 확인하지 못했습니다.',
        };
    } catch (error) {
        console.error('💥 [CRITICAL] 람다 내부 예외 발생:', error);
        return {
            success: false,
            statusCode: 500,
            message: `람다 런타임 오류: ${error.message}`,
        };
    } finally {
        const duration = Date.now() - startTime;
        console.log(`🏁 [FINISH] 실행 종료 (소요 시간: ${duration}ms)`);
        logMemory('Finished');

        if (browser) {
            await browser.close().catch(() => {});
        }
    }
};
