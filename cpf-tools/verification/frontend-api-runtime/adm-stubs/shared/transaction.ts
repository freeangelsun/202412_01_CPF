export const defaultHeaders:HeadersInit={"Accept":"application/json"};
let seq=0; export function createTransactionId(){seq++;return `123e4567-e89b-42d3-a456-${String(seq).padStart(12,"0")}`;}
export function isValidTransactionId(v:string|null){return !!v&&/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(v);}
