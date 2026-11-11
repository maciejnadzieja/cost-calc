export interface CatalogPage<T> {
    content: T[];
    page: {
        number: number;
        totalPages: number;
    };
}

export function catalogPageFromQuery(value: unknown): number {
    if (typeof value !== 'string' || !/^\d+$/.test(value)) return 0;
    const page = Number(value);
    return Number.isInteger(page) && page <= 2_147_483_647 ? page : 0;
}
