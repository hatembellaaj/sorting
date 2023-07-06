import dayjs from 'dayjs/esm';

export interface IArticle {
  id: string;
  title?: string | null;
  content?: string | null;
  createdat?: dayjs.Dayjs | null;
}

export type NewArticle = Omit<IArticle, 'id'> & { id: null };
