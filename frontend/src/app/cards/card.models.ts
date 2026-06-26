export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface GreetingCard {
  id: number;
  title: string;
  category: string;
  artist: string;
  price: number;
  stockStatus: StockStatus;
}

export interface CreateCardRequest {
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

export const STOCK_STATUS_OPTIONS: { value: StockStatus; label: string }[] = [
  { value: 'IN_STOCK', label: 'In stock' },
  { value: 'LOW_STOCK', label: 'Low stock' },
  { value: 'OUT_OF_STOCK', label: 'Out of stock' }
];
