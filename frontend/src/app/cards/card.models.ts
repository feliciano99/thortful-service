export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface GreetingCard {
  id: number;
  title: string;
  category: string;
  artist: string;
  price: number;
  stockStatus: StockStatus;
}

export interface CategoryOption {
  value: string;
  label: string;
}

export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: PageMetadata;
}

export interface CardQuery {
  page: number;
  size: number;
  sort: string;
  search?: string;
  category?: string | null;
}
