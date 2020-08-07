import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, MatRadioModule, MatDatepickerModule, MatTabsModule, MatCheckboxModule, MatCardModule, MatNativeDateModule } from '@angular/material';
import { ReactiveFormsModule, FormsModule } from '@angular/forms'; 
import { NgxPaginationModule } from 'ngx-pagination';

import { UsermangamentRoutingModule } from './usermangament-routing.module';
import { CreateuserComponent } from './createuser/createuser.component';
import { ManageuserComponent } from './manageuser/manageuser.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { UserSideMenuComponent } from './user-side-menu/user-side-menu.component';
import { UserPasswordChangeComponent } from './user-password-change/user-password-change.component';

import { UsermanagementService } from '@src/app/usermangament/services/usermanagement.service';

import { SortbyPipe } from './filters/sortby.pipe';
import { SearchTextPipe } from './filters/search-text.pipe';
import { TableDataFilterPipe } from './filters/table-data-filter.pipe';
import { BulkUserRegistrationComponent } from './bulk-user-registration/bulk-user-registration.component';
import { EditUserComponent } from './edit-user/edit-user.component';
import { EditUserDetailsComponent } from './edit-user-details/edit-user-details.component';

@NgModule({
  declarations: [CreateuserComponent, BulkUserRegistrationComponent ,ManageuserComponent, 
    ChangePasswordComponent, UserSideMenuComponent, UserPasswordChangeComponent, 
    SearchTextPipe, TableDataFilterPipe, SortbyPipe, EditUserComponent, EditUserDetailsComponent],
  imports: [
    CommonModule,
    UsermangamentRoutingModule,
    ReactiveFormsModule, FormsModule,
    MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, MatRadioModule, MatDatepickerModule, MatTabsModule, MatCheckboxModule, MatCardModule,
    NgxPaginationModule,
    MatNativeDateModule
  ],
  providers:[UsermanagementService]
})
export class UsermangamentModule { }
