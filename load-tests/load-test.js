import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
    scenarios: {
        create_shard: {
            executor: 'constant-arrival-rate',
            exec: 'createShard',
            rate: 1,
            timeUnit: '1s',
            duration: '15m',
            preAllocatedVUs: 2,
            maxVUs: 10,
        },
        update_shard: {
            executor: 'constant-arrival-rate',
            exec: 'updateShard',
            rate: 5,
            timeUnit: '1s',
            duration: '15m',
            preAllocatedVUs: 5,
            maxVUs: 20,
        },
        get_shard: {
            executor: 'constant-arrival-rate',
            exec: 'getShard',
            rate: 10,
            timeUnit: '1s',
            duration: '15m',
            preAllocatedVUs: 5,
            maxVUs: 20,
        },
    },
    thresholds: {
        'http_req_duration{endpoint:create}': ['p(95)<100'],
        'http_req_duration{endpoint:update}': ['p(95)<100'],
        'http_req_duration{endpoint:get}':    ['p(95)<100'],
        'http_req_failed{endpoint:create}':   ['rate<0.01'],
        'http_req_failed{endpoint:update}':   ['rate<0.01'],
        'http_req_failed{endpoint:get}':      ['rate<0.01'],
    },
};

export function setup() {
    const res = http.get(`${BASE_URL}/api/shard/sample?limit=10000`);
    if (res.status !== 200) {
        throw new Error(`Failed to fetch UUID pool: ${res.status} ${res.body}`);
    }
    const ids = res.json();
    console.log(`Loaded ${ids.length} UUIDs for the run`);
    return { ids };
}

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0;
        const v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

function randomShard() {
    return Math.floor(Math.random() * 1024);
}

function pickId(ids) {
    return ids[Math.floor(Math.random() * ids.length)];
}

export function createShard() {
    const id = uuidv4();
    const body = JSON.stringify({ shardIndex: randomShard() });
    const res = http.post(`${BASE_URL}/api/shard/create/${id}`, body, {
        headers: { 'Content-Type': 'application/json' },
        tags: { endpoint: 'create' },
    });
    check(res, { 'create 201': (r) => r.status === 201 });
}

export function updateShard(data) {
    const id = pickId(data.ids);
    const body = JSON.stringify({ shardIndex: randomShard() });
    const res = http.put(`${BASE_URL}/api/shard/update/${id}`, body, {
        headers: { 'Content-Type': 'application/json' },
        tags: { endpoint: 'update' },
    });
    check(res, { 'update 200': (r) => r.status === 200 });
}

export function getShard(data) {
    const id = pickId(data.ids);
    const res = http.get(`${BASE_URL}/api/shard/get/${id}`, {
        tags: { endpoint: 'get' },
    });
    check(res, { 'get 200': (r) => r.status === 200 });
}