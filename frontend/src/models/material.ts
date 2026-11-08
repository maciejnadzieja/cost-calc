export interface Material {
    id?: string | null;
    name: string;
    description: string;
    price: number;
    unit: string;
    category: string;
    sourceUrl?: string | null;
    imageUrl?: string | null;
}

export interface OnlineMaterialDraft {
    name: string;
    description: string;
    sourceUrl: string;
    imageUrl: string | null;
}

export function safeMaterialUrl(value: string | null | undefined): string | undefined {
    if (!value) return undefined;
    try {
        const url = new URL(value);
        if (url.protocol === 'https:' && !url.username && !url.password) return url.href;
    } catch {
        return undefined;
    }
}
