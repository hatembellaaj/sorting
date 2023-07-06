import dayjs from 'dayjs/esm';

import { IArticle, NewArticle } from './article.model';

export const sampleWithRequiredData: IArticle = {
  id: '98a873ee-1442-4553-9412-ea586fba6b1e',
};

export const sampleWithPartialData: IArticle = {
  id: 'c0d28bbc-dda2-4425-bfbc-490c9567b618',
  title: 'transform Highway cross-media',
};

export const sampleWithFullData: IArticle = {
  id: '4313f44f-21b5-44a4-adf0-bde51c88853c',
  title: 'Licensed',
  content: 'local enable deposit',
  createdat: dayjs('2023-07-06T06:26'),
};

export const sampleWithNewData: NewArticle = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
