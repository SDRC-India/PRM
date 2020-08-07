import { NgModule } from '@angular/core';
import { FormSearchPipe } from './form-search/form-search';
import { HomeFormSearchPipe } from './home-form-search/home-form-search';
@NgModule({
	declarations: [FormSearchPipe,
    HomeFormSearchPipe],
	imports: [],
	exports: [FormSearchPipe,
    HomeFormSearchPipe]
})
export class PipesModule {}
