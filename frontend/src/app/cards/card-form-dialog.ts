import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { CategoryOption, CreateCardRequest, GreetingCard, STOCK_STATUS_OPTIONS, StockStatus } from './card.models';
import { CardService } from './card.service';

interface ApiErrorBody {
  message?: string;
  fieldErrors?: { field: string; message: string }[];
}

@Component({
  selector: 'app-card-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule
  ],
  templateUrl: './card-form-dialog.html',
  styleUrl: './card-form-dialog.scss'
})
export class CardFormDialog {
  private readonly fb = inject(FormBuilder);
  private readonly cardService = inject(CardService);
  private readonly dialogRef = inject(MatDialogRef<CardFormDialog, GreetingCard>);

  protected readonly data = inject<{ categories: CategoryOption[] }>(MAT_DIALOG_DATA);
  protected readonly stockOptions = STOCK_STATUS_OPTIONS;
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    category: ['', Validators.required],
    artist: ['', [Validators.required, Validators.maxLength(150)]],
    price: [null as number | null, [Validators.required, Validators.min(0.01)]],
    stockStatus: [null as StockStatus | null, Validators.required]
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: CreateCardRequest = {
      title: value.title!.trim(),
      category: value.category!,
      artist: value.artist!.trim(),
      price: value.price!,
      stockStatus: value.stockStatus!
    };

    this.saving.set(true);
    this.serverError.set(null);
    this.cardService.create(request).subscribe({
      next: (card) => this.dialogRef.close(card),
      error: (err) => {
        this.saving.set(false);
        this.serverError.set(this.describeError(err));
      }
    });
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private describeError(err: unknown): string {
    const body = (err as { error?: ApiErrorBody })?.error;
    if (body?.fieldErrors?.length) {
      return body.fieldErrors.map((fieldError) => `${fieldError.field}: ${fieldError.message}`).join(', ');
    }
    return body?.message ?? 'Could not create the card. Please try again.';
  }
}
